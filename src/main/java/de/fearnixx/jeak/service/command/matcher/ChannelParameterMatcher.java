package de.fearnixx.jeak.service.command.matcher;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.spec.matcher.IParameterMatcher;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChannelParameterMatcher implements IParameterMatcher<IChannel> {

    private static final Pattern ID_PATTERN = Pattern.compile("\\d+");

    private static final Logger logger = LoggerFactory.getLogger(ChannelParameterMatcher.class);

    @Inject
    private IDataCache dataCache;

    @Override
    public Class<IChannel> getSupportedType() {
        return IChannel.class;
    }

    @Override
    public Optional<IChannel> tryMatch(String paramString) {
        if (ID_PATTERN.matcher(paramString).matches()) {
            IChannel channel = dataCache.getChannelMap().getOrDefault(Integer.parseInt(paramString), null);
            if (channel != null) {
                return Optional.of(channel);
            }
        } else {
            List<IChannel> result = dataCache.getChannels()
                    .stream()
                    .filter(c -> c.getName().contains(paramString))
                    .collect(Collectors.toList());

            if (result.size() > 1) {
                logger.warn("Multiple results found for user input: {}", paramString);
            } else if (result.size() == 1) {
                return Optional.of(result.get(0));
            }
        }
        return Optional.empty();
    }
}
