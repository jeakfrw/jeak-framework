package de.fearnixx.jeak.service.http.request.auth.token;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.http.request.token.IAuthenticationToken;
import de.fearnixx.jeak.service.http.request.token.ITokenAuthService;
import de.fearnixx.jeak.service.permission.base.ISubject;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IUser;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

public class TokenAuthService extends Configurable implements ITokenAuthService {

    private static final Logger logger = LoggerFactory.getLogger(TokenAuthService.class);
    private static final String NO_EXPIRY_VALUE = "never";
    public static final String EXPIRY_NODE_NAME = "expiry";
    public static final DateTimeFormatter EXPIRY_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Inject
    @Config(category = "rest", id = "token-auth")
    private IConfig tokenConfig;

    @Inject
    private IUserService userService;

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

    @Nonnull
    @Override
    public synchronized IAuthenticationToken generateToken(IUser tokenOwner) {
        Objects.requireNonNull(tokenOwner, "Token owner may not be null!");
        var tokenStr = UUID.randomUUID().toString().replace("-", "");
        var tokenInstance = new AuthenticationToken(tokenStr, tokenOwner, null);

        var ownerNode = getTokensNode().getNode(tokenOwner.getUniqueID().toString());
        var tokenNode = ownerNode.getNode(tokenStr);
        tokenNode.getNode(EXPIRY_NODE_NAME).setString(NO_EXPIRY_VALUE);

        logger.info("Generated new authentication token for subject '{}': {}", tokenOwner, tokenStr);
        return tokenInstance;
    }

    @Override
    public synchronized void setTokenExpiry(@Nonnull IAuthenticationToken token, @Nullable ZonedDateTime expiryValue) {
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
    public synchronized void revokeTokens(ISubject subject) {
        revokeTokens(subject.getUniqueID().toString());
    }

    protected synchronized void revokeTokens(String subjectUUID) {
        logger.info("Revoking tokens of subject '{}'", subjectUUID);
        getTokensNode().remove(subjectUUID);
    }

    @Listener(order = Listener.Orders.EARLY)
    public synchronized void onInitialize(IBotStateEvent.IInitializeEvent event) {
        if (!loadConfig()) {
            event.cancel();
        }
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
