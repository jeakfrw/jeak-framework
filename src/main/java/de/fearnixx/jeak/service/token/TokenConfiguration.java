package de.fearnixx.jeak.service.token;

import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import de.mlessmann.confort.node.ConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TokenConfiguration extends Configurable {
    private static final Logger logger = LoggerFactory.getLogger(TokenConfiguration.class);
    private static final String DEFAULT_TOKEN_CONFIG = "/restService/token/defaultToken.json";

    @Inject
    @Config(id = "tokens")
    private IConfig configRef;

    public TokenConfiguration() {
        super(TokenConfiguration.class);
    }

    @Override
    public boolean loadConfig() {
        return super.loadConfig();
    }

    @Override
    protected void onDefaultConfigLoaded() {
        saveConfig();
    }

    @Override
    protected IConfig getConfigRef() {
        return configRef;
    }

    @Override
    protected String getDefaultResource() {
        return DEFAULT_TOKEN_CONFIG;
    }

    @Override
    protected boolean populateDefaultConf(IConfigNode root) {
        return false;
    }

    /**
     * Check for the {@link TokenScope} of a given token.
     *
     * @param token The token to find the scopes for.
     * @return An instance of {@link TokenScope} with the scopes of the token.
     */
    public TokenScope getTokenScopes(String token) {
        logger.debug(MessageFormat.format("reading token {0}", token));
        IConfigNode tokenNode = getConfig().getNode(token);
        Set<String> tokenScopes = new HashSet<>();
        if (!tokenNode.isVirtual()) {
            Optional<List<IConfigNode>> nodes = tokenNode.optList();
            nodes.ifPresent(localIConfigNodes ->
                    localIConfigNodes.stream()
                            .map(IConfigNode::optString)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(tokenScopes::add)
            );
        }
        return new TokenScope(tokenScopes);
    }

    public void saveToken(String token, TokenScope tokenScope) {
        ConfigNode child = new ConfigNode();
        child.setList();
        child.appendValue(tokenScope.getScopeSet());
        getConfig().put(token, child);
    }
}
