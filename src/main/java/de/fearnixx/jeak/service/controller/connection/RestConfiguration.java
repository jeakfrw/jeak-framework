package de.fearnixx.jeak.service.controller.connection;

import de.fearnixx.jeak.reflect.Config;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.util.Configurable;
import de.mlessmann.confort.api.IConfig;
import de.mlessmann.confort.api.IConfigNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RestConfiguration extends Configurable {
    private static final Logger logger = LoggerFactory.getLogger(RestConfiguration.class);
    private static final String DEFAULT_TOKEN_CONFIG = "/restService/config.json";

    private static final String HTTPS_CONFIG = "https";
    private static final String HTTPS_ENABLED = "https-enabled";
    private static final String HTTPS_REJECT_UNENCRYPTED = "reject-unencrypted";
    public static final boolean DEFAULT_HTTPS_REJECT_UNENCRYPTED = true;
    private static final String HTTPS_BEHIND_PROXY = "behind-ssl-proxy";
    private static final String HTTPS_KEYSTORE_PATH = "keystore-path";
    private static final String HTTPS_KEYSTORE_PASSWORD = "keystore-password";

    private static final String HTTPS_TRUSTSTORE_PATH = "truststore-path";
    private static final String HTTPS_TRUSTSTORE_PASSWORD = "truststore-password";

    private static final String HEADER_CONFIG = "headers";

    private static final String CORS_CONFIG = "cors";
    private static final String CORS_CONFIG_ENABLED = "enabled";

    @Inject
    @Config(id = "rest")
    private IConfig configRef;

    public RestConfiguration() {
        super(RestConfiguration.class);
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

    public Optional<Integer> getPort() {
        return getConfig().getNode("port").optInteger();
    }

    public Map<String, String> getHeaders() {
        logger.debug("Loading headers");
        Map<String, String> header = new HashMap<>();
        getConfig().getNode(HEADER_CONFIG)
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((s, iConfigNode) -> header.put(s, iConfigNode.asString()));
        return header;
    }

    private <T> Optional<T> getValueFromHttpsConfig(String value, Class<T> type) {
        return Optional.ofNullable(getConfig().getNode(HTTPS_CONFIG).optValueMap(type).get(value));
    }

    public Optional<Boolean> isHttpsEnabled() {
        return getValueFromHttpsConfig(HTTPS_ENABLED, Boolean.class);
    }

    public Optional<Boolean> isBehindSslProxy() {
        return getValueFromHttpsConfig(HTTPS_BEHIND_PROXY, Boolean.class);
    }

    public Optional<Boolean> rejectUnencryptedTraffic() {
        return getValueFromHttpsConfig(HTTPS_REJECT_UNENCRYPTED, Boolean.class);
    }

    public Optional<String> getHttpsKeystorePath() {
        return getValueFromHttpsConfig(HTTPS_KEYSTORE_PATH, String.class);
    }

    public Optional<String> getHttpsKeystorePassword() {
        return getValueFromHttpsConfig(HTTPS_KEYSTORE_PASSWORD, String.class);
    }

    public Optional<String> getHttpsTruststorePath() {
        return getValueFromHttpsConfig(HTTPS_TRUSTSTORE_PATH, String.class);
    }

    public Optional<String> getHttpsTruststorePassword() {
        return getValueFromHttpsConfig(HTTPS_TRUSTSTORE_PASSWORD, String.class);
    }

    public boolean isCorsEnabled() {
        Optional<Map<String, IConfigNode>> cors = getConfig().getNode(CORS_CONFIG)
                .optMap();
        if (!cors.isPresent()) {
            return false;
        }
        return cors.get().get(CORS_CONFIG_ENABLED).asBoolean();
    }

    public Map<String, String> getCorsHeaders() {
        Map<String, String> header = new HashMap<>();
        getConfig().getNode(CORS_CONFIG, HEADER_CONFIG)
                .optMap()
                .orElseGet(Collections::emptyMap)
                .forEach((s, iConfigNode) -> header.put(s, iConfigNode.asString()));
        return header;
    }
}
