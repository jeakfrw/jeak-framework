package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.matcher.BasicMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatcherResponse;
import de.fearnixx.jeak.service.command.spec.matcher.IMatchingContext;
import de.fearnixx.jeak.service.command.spec.matcher.MatcherResponseType;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChannelParameterMatcher extends AbstractFrameworkTypeMatcher<IChannel> {

    private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

    private static final Logger logger = LoggerFactory.getLogger(ChannelParameterMatcher.class);

    @Inject
    private IDataCache dataCache;

    @Override
    public Class<IChannel> getSupportedType() {
        return IChannel.class;
    }

    @Override
    public IMatcherResponse parse(ICommandExecutionContext ctx, IMatchingContext matchingContext, String extracted) {
        String name = matchingContext.getArgumentOrParamName();
        if (ID_PATTERN.matcher(extracted).matches()) {
            IChannel channel = dataCache.getChannelMap().getOrDefault(Integer.parseInt(extracted), null);
            if (channel != null) {
                logger.debug("Found channel parameter: \"{}\" --> {}", extracted, channel);
                ctx.putOrReplaceOne(name, channel);
                ctx.putOrReplaceOne(name + "Id", channel.getID());
                ctx.getParameterIndex().incrementAndGet();
                return BasicMatcherResponse.SUCCESS;
            }
        } else {
            List<IChannel> result = dataCache.getChannels()
                    .stream()
                    .filter(c -> c.getName().contains(extracted))
                    .collect(Collectors.toList());

            if (result.size() == 1) {
                IChannel channel = result.get(0);
                logger.debug("Found channel parameter: \"{}\" --> {}", extracted, channel);
                ctx.putOrReplaceOne(name, channel);
                ctx.putOrReplaceOne(name + "Id", channel.getID());
                ctx.getParameterIndex().incrementAndGet();
                return BasicMatcherResponse.SUCCESS;

            } else if (result.size() > 1) {
                String allChannels =
                        result.stream()
                                .map(c -> c.getName() + '/' + c.getID())
                                .collect(Collectors.joining(","));
                logger.debug("Channel parameter ambiguous: \"{}\" --> [{}]", extracted, allChannels);
                String ambiguityMessage = getLocaleUnit()
                        .getContext(ctx.getSender().getCountryCode())
                        .getMessage("matcher.type.ambiguousSearch",
                                Map.of("results", allChannels));
                return new BasicMatcherResponse(MatcherResponseType.ERROR, ctx.getParameterIndex().get(), ambiguityMessage);
            }
        }

        return getIncompatibleTypeResponse(ctx, matchingContext, extracted);
    }
}
