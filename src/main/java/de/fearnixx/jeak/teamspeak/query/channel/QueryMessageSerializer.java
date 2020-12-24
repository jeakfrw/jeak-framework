package de.fearnixx.jeak.teamspeak.query.channel;

import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryEncoder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Magnus Leßmann
 * @since 1.2.0
 */
public class QueryMessageSerializer {

    private QueryMessageSerializer() {
    }

    /**
     * Serializes a {@link IQueryRequest} into a message string for telnet-like TeamSpeak connections.
     */
    public static String serialize(IQueryRequest request) {
        final var message = new StringBuilder();

        // Append: Command
        if (request.getCommand().length() > 0) {
            message.append(request.getCommand()).append(' ');
        }

        // Append: Objects
        // (Chain of `key=val key2=val2...` separated by '|')
        List<IDataHolder> dataChain = request.getDataChain();
        final int chainLength = dataChain.size();

        for (int i = 0; i < chainLength; i++) {
            // Copy the mapping in order to avoid concurrent modification
            Map<String, String> properties = new LinkedHashMap<>(dataChain.get(i).getValues());
            String[] keys = properties.keySet().toArray(new String[0]);

            // Skip empty chain elements
            if (keys.length == 0) {
                continue;
            } else if (i > 0) {
                message.append('|');
            }

            for (int j = 0; j < keys.length; j++) {
                String propKey = keys[j];
                int keyLen = propKey.length();
                char[] propKeyParts = new char[keyLen];
                propKey.getChars(0, keyLen, propKeyParts, 0);

                String propValue = properties.get(keys[j]);
                int valLen = propValue.length();
                char[] propValParts = new char[valLen];
                propValue.getChars(0, valLen, propValParts, 0);

                char[] encodedKeyParts = QueryEncoder.encodeBuffer(propKeyParts);
                char[] encodedValParts = QueryEncoder.encodeBuffer(propValParts);
                message.append(encodedKeyParts)
                        .append('=')
                        .append(encodedValParts);

                if (j < keys.length - 1) {
                    message.append(' ');
                }
            }
        }

        // Append: Options
        request.getOptions().forEach(option -> {
            if (message.length() > 0)
                message.append(' ');
            message.append(option);
        });

        return message.toString();
    }
}
