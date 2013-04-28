package com.akrivos.eos.http.constants;

/**
 * An enum with all the HTTP methods.
 */
public enum HttpMethod {
    /**
     * Request a representation of the resource.
     */
    GET,

    /**
     * Request only the headers for the resource.
     */
    HEAD,

    /**
     * Process the request body with the resource
     */
    POST,

    /**
     * Create or update a new resource with the contents of the request body.
     */
    PUT,

    /**
     * Remove the specified resource.
     */
    DELETE,

    /**
     * Return the HTTP methods the specified resource supports.
     */
    OPTIONS,

    /**
     * Echo the received request.
     */
    TRACE,

    /**
     * Convert the connection to a transparent tcp/ip tunnel, usually
     * to allow SSL/TLS through an unencrypted HTTP proxy.
     */
    CONNECT;

    /**
     * Keep a local copy of values since it's not going to be mutated.
     * http://stackoverflow.com/questions/1167982/check-if-enum-exists-in-java
     */
    private static final HttpMethod[] copyOfValues = values();

    /**
     * Parses a {@link String} and tries to match the {@link HttpMethod}.
     *
     * @param name the {@link String} representation of an {@link HttpMethod}.
     * @return the {@link HttpMethod} if found, null otherwise.
     */
    public static HttpMethod forName(String name) {
        for (HttpMethod value : copyOfValues) {
            if (value.name().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}