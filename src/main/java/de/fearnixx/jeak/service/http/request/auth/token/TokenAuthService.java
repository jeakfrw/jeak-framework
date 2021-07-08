package de.fearnixx.jeak.service.http.request.auth.token;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.http.request.IRequestContext;
import de.fearnixx.jeak.service.http.request.token.IAuthenticationToken;
import de.fearnixx.jeak.service.http.request.token.ITokenAuthService;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import static de.fearnixx.jeak.service.command.spec.Commands.commandSpec;
import static de.fearnixx.jeak.service.command.spec.Commands.paramSpec;

public class TokenAuthService extends Configurable implements ITokenAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthService.class);
    private static final int TOKEN_LENGTH = Main.getProperty("jeak.http.auth.token_length", 128);
    private static final String AUTHENTICATION_TOKEN_PERMISSION = "jeak.command.http.authToken";
    public static final String AUTHENTICATION_TOKEN_PERMISSION_BASE = AUTHENTICATION_TOKEN_PERMISSION + ".base";
    public static final String AUTHENTICATION_TOKEN_PERMISSION_OTHER = AUTHENTICATION_TOKEN_PERMISSION + ".other";
    private static final String MSG_TOKEN_GENERATED = "http.auth.token.generated";

    private static final String NO_EXPIRY_VALUE = "never";
    public static final String EXPIRY_NODE_NAME = "expiry";
    public static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private static final Pattern HEADER_EXTRACTION_PATTERN = Pattern.compile("Token (.+)$");

    private final RandomString tokenGenerator = new RandomString(TOKEN_LENGTH);

    @Inject
    @Config(category = "rest", id = "token-auth")
    private IConfig tokenConfig;

    @Inject
    private IUserService userService;

    @Inject
    private ICommandService commandService;

    @Inject
    @LocaleUnit(value = "jeak.http", defaultResource = "localization/http.json")
    private ILocalizationUnit localizationUnit;

    public TokenAuthService() {
        super(TokenAuthService.class);
    }

    @Override
    protected IConfig getConfigRef() {
        return tokenConfig;
    }

    protected IConfigNode getTokensNode() {
        return getConfig().getNode("tokens");
    }

    public synchronized boolean attemptAuthentication(Request request) {
        var authHeader = request.headers("Authorization");
        if (authHeader == null || authHeader.isBlank()) {
            logger.debug("Request without Auth-Header.");
            return false;
        }

        var matcher = HEADER_EXTRACTION_PATTERN.matcher(authHeader);
        if (!matcher.find()) {
            logger.debug("Token scheme not found in header: {}", authHeader);
            return false;
        }

        String token = matcher.group(1);
        var optTokenMapEntry = getTokensNode().optMap()
                .orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .filter(pair -> !pair.getValue().getNode(token).isVirtual())
                .findAny();
        if (optTokenMapEntry.isEmpty()) {
            logger.debug("No match found for token: {}", token);
            return false;
        }

        String userUID = optTokenMapEntry.get().getKey();
        var tokenExpiryStr = optTokenMapEntry.get().getValue().getNode(token, EXPIRY_NODE_NAME).optString("");
        ZonedDateTime expiry = null;
        if (!NO_EXPIRY_VALUE.equals(tokenExpiryStr)) {
            try {
                expiry = ZonedDateTime.parse(tokenExpiryStr, EXPIRY_FORMAT);
                if (ZonedDateTime.now().isAfter(expiry)) {
                    logger.warn("Access with expired token: '{}' -> '{}'.", userUID, token);
                    return false;
                }

            } catch (DateTimeParseException e) {
                logger.warn("Cannot read expiry value of token: '{}' -> '{}'", userUID, token, e);
                return false;
            }
        }

        var user = userService.findUserByUniqueID(userUID)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Token owner of token " + token + " could not be found! (" + userUID + ")"));
        AuthenticationToken tokenInstance = new AuthenticationToken(token, user, expiry);
        request.attribute(IRequestContext.Attributes.AUTHENTICATION_TOKEN, tokenInstance);
        request.attribute(IRequestContext.Attributes.AUTHENTICATION_USER, user);
        return true;
    }

    @Override
    public synchronized IAuthenticationToken generateToken(IUser tokenOwner) {
        Objects.requireNonNull(tokenOwner, "Token owner may not be null!");
        var tokenStr = tokenGenerator.nextString();
        var tokenInstance = new AuthenticationToken(tokenStr, tokenOwner, null);

        var ownerNode = getTokensNode().getNode(tokenOwner.getClientUniqueID());
        var tokenNode = ownerNode.getNode(tokenStr);
        tokenNode.getNode(EXPIRY_NODE_NAME).setString(NO_EXPIRY_VALUE);

        logger.info("Generated new authentication token for subject '{}': {}", tokenOwner, tokenStr);
        return tokenInstance;
    }

    @Override
    public synchronized void setTokenExpiry(IAuthenticationToken token, ZonedDateTime expiryValue) {
        Objects.requireNonNull(token, "Provided token cannot be null.");

        var tokenNode = getTokensNode().getNode(token.getTokenString());
        if (tokenNode.isVirtual()) {
            throw new IllegalArgumentException("Token '" + token.getTokenString() + "' does not exist!");
        }
        if (expiryValue == null) {
            tokenNode.getNode(EXPIRY_NODE_NAME).setString(NO_EXPIRY_VALUE);
            logger.debug("Set token expiry of '{}' to: {}", token.getTokenString(), NO_EXPIRY_VALUE);
        } else if (ZonedDateTime.now().isAfter(expiryValue)) {
            logger.warn("Revoking token '{}' as expiry was set to past.", token.getTokenString());
            revokeToken(token);
        } else {
            tokenNode.getNode(EXPIRY_NODE_NAME).setString(expiryValue.format(EXPIRY_FORMAT));
        }
    }

    @Override
    public synchronized void revokeToken(IAuthenticationToken token) {
        var optEntry = getTokensNode().optMap().orElseGet(Collections::emptyMap)
                .entrySet()
                .stream()
                .filter(e -> !e.getValue().getNode(token.getTokenString()).isVirtual())
                .findFirst();

        if (optEntry.isEmpty()) {
            logger.debug("Cannot remove token '{}'. Not found!", token.getTokenString());
        } else {
            var subject = optEntry.get().getKey();
            logger.info("Revoking token '{}' of subject '{}'.", token.getTokenString(), subject);
            getTokensNode().getNode(subject).remove(token.getTokenString());
        }
    }

    @Override
    public synchronized void revokeTokens(IUser tokenOwner) {
        revokeTokens(tokenOwner.getClientUniqueID());
    }

    protected synchronized void revokeTokens(String ts3uid) {
        logger.info("Revoking tokens of subject '{}'", ts3uid);
        getTokensNode().remove(ts3uid);
    }

    @Listener(order = Listener.Orders.EARLY)
    public synchronized void onInitialize(IBotStateEvent.IInitializeEvent event) {
        if (!loadConfig()) {
            event.cancel();
        }

        // Register commands
        commandService.registerCommand(
                commandSpec("auth-token", "http:auth-token", "http:authenticationToken")
                        .permission(AUTHENTICATION_TOKEN_PERMISSION_BASE)
                        .parameters(paramSpec().optional(paramSpec("user", IUser.class)))
                        .executor(this::onUserRequestedPermission)
                        .build());
    }

    @Listener(order = Listener.Orders.EARLIER)
    public synchronized void onShutdown(IBotStateEvent.IPreShutdown shutdownEvent) {
        if (!saveConfig()) {
            logger.error("Configuration save failed, see preceding error.");
        }
    }

    protected void onUserRequestedPermission(ICommandExecutionContext execCtx) {
        var optTarget = execCtx.getOne("user", IUser.class);
        if (optTarget.isPresent() && !execCtx.getSender().hasPermission(AUTHENTICATION_TOKEN_PERMISSION_OTHER)) {
            execCtx.getSender().sendMessage(String.format("You're not allowed to request tokens for others (%s)", AUTHENTICATION_TOKEN_PERMISSION_OTHER));
            return;
        }

        var generatedToken = generateToken(optTarget.orElseGet(execCtx::getSender));
        String notifyMessage = localizationUnit.getContext(execCtx.getSender())
                .getMessage(MSG_TOKEN_GENERATED, Map.of("token", generatedToken.getTokenString()));
        execCtx.getSender().sendMessage(notifyMessage);
    }

    @Override
    protected boolean populateDefaultConf(IConfigNode root) {
        root.getNode("tokens").setMap();
        return true;
    }

    @Override
    protected String getDefaultResource() {
        return null;
    }
}
