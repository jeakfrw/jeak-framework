package de.fearnixx.jeak.controller.connection;

public class EndpointBuilder {
    private StringBuilder tmpEndpoint;
    private static final char PART_DELIMITER = '/';

    public EndpointBuilder() {
        initTmpEndpoint();
    }

    private void initTmpEndpoint() {
        this.tmpEndpoint = new StringBuilder();
    }

    public EndpointBuilder add(String part) {
        this.tmpEndpoint.append(PART_DELIMITER);
        this.tmpEndpoint.append(cleanUpString(part));
        return this;
    }

    public EndpointBuilder clear() {
        initTmpEndpoint();
        return this;
    }

    public String build() {
        return tmpEndpoint.toString();
    }

    private String cleanUpString(String string) {
        String createdString = string;
        if (string.charAt(string.length() - 1) == PART_DELIMITER) {
            createdString = string.substring(0, createdString.length() - 2);
        }
        if (string.charAt(0) == PART_DELIMITER) {
            createdString = string.substring(1);
        }
        return createdString;
    }
}
