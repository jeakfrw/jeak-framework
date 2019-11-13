package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
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

public class ClientParameterMatcher extends AbstractFrameworkTypeMatcher<IClient> {

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
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        String parameterName = matchingContext.getArgumentOrParamName();
        List<IClient> results = Collections.emptyList();
        if (DBID_PATTERN.matcher(extracted).matches()) {
            results = userService.findClientByDBID(Integer.parseInt(extracted));

        } else if (CLID_PATTERN.matcher(extracted).matches()) {
            IClient res = dataCache.getClientMap().getOrDefault(Integer.parseInt(extracted), null);
            if (res != null) {
                results = List.of(res);
            }
        } else if (TSUID_PATTERN.matcher(extracted).matches()) {
            results = userService.findClientByUniqueID(extracted);
        } else {
            if (extracted.length() < MAX_NICKNAME_LENGTH) {
                results = userService.findClientByNickname(extracted);
            } else {
                logger.warn("Input parameter is too long for a TS3 client name: {}", extracted);
            }
        }

        if (results.size() == 1) {
            ctx.putOrReplaceOne(parameterName, results.get(0));
            ctx.putOrReplaceOne(parameterName + "Id", results.get(0).getClientID());
            ctx.getParameterIndex().incrementAndGet();
            return BasicMatcherResponse.SUCCESS;

        } else if (!results.isEmpty()) {
            String names =
                    results.stream().map(IClient::toString).collect(Collectors.joining(", "));
            String ambiguityMessage =
                    getLocaleUnit().getContext(ctx.getSender().getCountryCode())
                            .getMessage("matcher.type.ambiguousSearch", Map.of("results", names));
            return new BasicMatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), ambiguityMessage);
        }
        return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
    }
}
