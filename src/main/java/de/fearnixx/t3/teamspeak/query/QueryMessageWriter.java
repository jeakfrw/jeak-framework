package de.fearnixx.t3.teamspeak.query;

import de.fearnixx.t3.teamspeak.data.IDataHolder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryMessageWriter {

    private boolean autoFlush = true;
    private final OutputStreamWriter writer;

    public QueryMessageWriter(OutputStream outputStream) {
        writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
    }

    public void writeMessage(IQueryRequest request) throws IOException {
        writer.write(buildSocketMessage(request));

        if (autoFlush) {
            flush();
        }
    }

    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Builds the message for the TS3 query from a request.
     */
    private String buildSocketMessage(IQueryRequest request) {
        StringBuilder sockMessage = new StringBuilder();

        // Append: Command
        if (request.getCommand().length() > 0) {
            sockMessage.append(request.getCommand()).append(' ');
        }

        // Append: Objects
        // (Chain of `key=val key2=val2...` separated by '|')
        List<IDataHolder> dataChain = request.getDataChain();
        final int chainLength = dataChain.size();
        final int chainLastIndex = chainLength - 1;

        for (int i = 0; i < chainLength; i++) {
            // Copy the mapping in order to avoid concurrent modification
            Map<String, String> properties = new HashMap<>(dataChain.get(i).getValues());
            String[] keys = properties.keySet().toArray(new String[0]);

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
                sockMessage.append(encodedKeyParts)
                        .append('=')
                        .append(encodedValParts);

                if (j < keys.length-1) {
                    sockMessage.append(' ');
                }
            }

            if (i < chainLastIndex) {
                sockMessage.append('|');
            }
        }

        // Append: Options
        request.getOptions().forEach(o -> {
            if (sockMessage.length() > 0)
                sockMessage.append(' ');
            sockMessage.append(o);
        });

        return sockMessage.toString();
    }

    public boolean isAutoFlush() {
        return autoFlush;
    }

    public void setAutoFlush(boolean autoFlush) {
        this.autoFlush = autoFlush;
    }
}
