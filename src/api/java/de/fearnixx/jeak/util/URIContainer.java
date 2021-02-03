package de.fearnixx.jeak.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class URIContainer {

    /**
     * Factory method for known-to-be-legal string URIs.
     *
     * @see URI#create(String)
     */
    public static URIContainer ofLegal(String uriAsString) {
        return of(URI.create(uriAsString));
    }

    /**
     * Factory method for maybe-legal string URIs.
     *
     * @throws URISyntaxException when the provided string is not a valid URI.
     * @see URI#URI(String)
     */
    public static URIContainer of(String uriAsString) throws URISyntaxException {
        return of(new URI(uriAsString));
    }

    /**
     * @throws URISyntaxException When the URL did not contain a valid URI.
     * @see URL#toURI()
     */
    public static URIContainer of(URL url) throws URISyntaxException {
        return of(url.toURI());
    }

    /**
     * Creates (and parses) a new URIContainer from the given URI.
     */
    public static URIContainer of(URI uri) {
        return of(uri, StandardCharsets.UTF_8);
    }

    /**
     * Creates (and parses) a new URIContainer from the given URI.
     */
    public static URIContainer of(URI uri, Charset encodingCharset) {
        final Map<String, List<String>> parameters = new LinkedHashMap<>();
        if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
            Arrays.stream(uri.getRawQuery().split("&"))
                    .map(it -> splitParameter(it, encodingCharset))
                    .forEach(it -> parameters.computeIfAbsent(it[0], key -> new LinkedList<>()).add(it[1]));
        }

        return new URIContainer(uri, parameters);
    }

    private static String[] splitParameter(String it, Charset encodingCharset) {
        final int eqIdx = it.indexOf('=');
        final String key = URLDecoder.decode(
                eqIdx > 0 ? it.substring(0, eqIdx) : it, encodingCharset
        );
        final String value = eqIdx > 0 && it.length() > eqIdx + 1 ?
                URLDecoder.decode(
                        it.substring(eqIdx + 1), encodingCharset
                )
                : null;
        return new String[]{key, value};
    }

    private final URI originalUri;
    private final Map<String, List<String>> queryParameters;

    protected URIContainer(URI originalUri, Map<String, List<String>> queryParameters) {
        this.originalUri = originalUri;
        this.queryParameters = queryParameters;
    }

    /**
     * The original URI object this container was created from.
     */
    public URI getOriginalUri() {
        return originalUri;
    }

    /**
     * A mapping of the query parameters from this URI.
     */
    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    /**
     * Returns all values associated with the provided parameter key.
     */
    public Optional<List<String>> optManyQuery(String paramKey) {
        return Optional.ofNullable(getQueryParameters().getOrDefault(paramKey, null));
    }

    /**
     * Returns the first value associated with the provided parameter key.
     */
    public Optional<String> optSingleQuery(String paramKey) {
        return optManyQuery(paramKey).map(l -> l.get(0));
    }

    /**
     * Returns whether or not a query parameter with this key is present.
     */
    public boolean hasQuery(String paramKey) {
        return optManyQuery(paramKey).isPresent();
    }

    /**
     * Returns whether or not more than one query parameter value is known for this key.
     */
    public boolean hasMultipleQuery(String paramKey) {
        return optManyQuery(paramKey).map(l -> l.size() > 1).orElse(false);
    }

    @Override
    public String toString() {
        return "URIContainer{" +
                "originalUri=" + originalUri +
                '}';
    }
}
