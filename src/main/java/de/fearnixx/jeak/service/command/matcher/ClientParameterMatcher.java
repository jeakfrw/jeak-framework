package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class ClientParameterMatcher implements IParameterMatcher<IClient> {

    private static final Pattern DBID_PATTERN = Pattern.compile("\\d+");
    private static final Pattern CLID_PATTERN = Pattern.compile("c:\\d+");
    private static final Pattern TSUID_PATTERN = Pattern.compile("[a-zA-Z0-9/_]{20,27}=");
    private static final Integer MAX_NICKNAME_LENGTH = 100;

    private static final Logger logger = LoggerFactory.getLogger(ClientParameterMatcher.class);

    @Inject
    private IUserService userService;

    @Inject
    private IDataCache dataCache;

    @Override
    public Class<IClient> getSupportedType() {
        return IClient.class;
    }

    @Override
    public Optional<IClient> tryMatch(String paramString) {
        List<IClient> results = Collections.emptyList();
        if (DBID_PATTERN.matcher(paramString).matches()) {
            results = userService.findClientByDBID(Integer.parseInt(paramString));

        } else if (CLID_PATTERN.matcher(paramString).matches()) {
            IClient res = dataCache.getClientMap().getOrDefault(Integer.parseInt(paramString), null);
            if (res != null) {
                results = List.of(res);
            }
        } else if (TSUID_PATTERN.matcher(paramString).matches()) {
            results = userService.findClientByUniqueID(paramString);
        } else {
            if (paramString.length() < MAX_NICKNAME_LENGTH) {
                results = userService.findClientByNickname(paramString);
            } else {
                logger.warn("Input parameter is too long for a TS3 client name: {}", paramString);
            }
        }

        if (results.size() == 1) {
            return Optional.of(results.get(0));
        } else if (!results.isEmpty()) {
            logger.info("Found multiple results for user param: {}", paramString);
        }
        return Optional.empty();
    }
}
