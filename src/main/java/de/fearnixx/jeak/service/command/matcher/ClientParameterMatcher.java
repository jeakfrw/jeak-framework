package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.CommandExecutionContext;
import de.fearnixx.jeak.service.command.matcher.meta.MatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClientParameterMatcher extends AbstractTypeMatcher<IClient> {

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
    public IMatcherResponse tryMatch(CommandExecutionContext ctx, int startParamPosition, String parameterName) {
        String paramString = ctx.getArguments().get(startParamPosition);
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
            ctx.getParameters().put(parameterName, results.get(0));

        } else if (!results.isEmpty()) {
            String names =
                    results.stream().map(IClient::toString).collect(Collectors.joining(", "));
            String ambiguityMessage =
                    getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                            .getMessage("matcher.type.ambiguousSearch", Map.of("results", names));
            return new MatcherResponse(MatcherResponseType.ERROR, startParamPosition, ambiguityMessage);
        }
        return getIncompatibleTypeResponse(ctx, startParamPosition);
    }
}
