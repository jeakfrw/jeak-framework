package de.fearnixx.jeak.controller.connection;

public class EndpointBuilder {
    private StringBuilder tmpEndpoint;
    private static final String PART_DELIMITER = "/";

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
        return string
                .replace(PART_DELIMITER, "");
    }
}
