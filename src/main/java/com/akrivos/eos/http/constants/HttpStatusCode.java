package com.akrivos.eos.http.constants;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * An enum with all HTTP status codes along with their reason-phrase.
 * https://github.com/bigcompany/know-your-http
 */
public enum HttpStatusCode {
    // - 1xx: Informational
    // - Request received, continuing process
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),

    // - 2xx: Success
    // - The action was successfully received, understood, and accepted
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET(205, "Reset Content"),
    PARTIAL(206, "Partial Content"),

    // - 3xx: Redirection
    // - Further action must be taken in order to complete the request
    MULT_CHOICE(300, "Multiple Choices"),
    MOVED_PERM(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMP_REDIRECT(307, "Temporary Redirect"),

    // - 4xx: Client Error
    // - The request contains bad syntax or cannot be fulfilled
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTH(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECON_FAILED(412, "Precondition Failed"),
    ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQ_TOO_LONG(414, "Request-URI Too Long"),
    UNSUPPORTED_TYPE(415, "Unsupported Media Type"),
    RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),

    // - 5xx: Server Error
    // - The server failed to fulfill an apparently valid request
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION(505, "HTTP Version Not Supported");

    /**
     * Keep a map containing all the enums with their {@link Integer} value.
     * http://stackoverflow.com/questions/1167982/check-if-enum-exists-in-java
     */
    private static final Map<Integer, HttpStatusCode> intToValueMap =
            new HashMap<Integer, HttpStatusCode>();
    private final int statusCode;
    private final String reasonPhrase;

    /**
     * Populates the map using EnumSet.allOf, which is much more efficient for
     * enums without a large number of elements.
     */
    static {
        for (HttpStatusCode value : EnumSet.allOf(HttpStatusCode.class)) {
            intToValueMap.put(value.getStatusCode(), value);
        }
    }

    /**
     * Creates a new {@link HttpStatusCode} with the specified
     * status code and reason-phrase.
     *
     * @param statusCode   the status code.
     * @param reasonPhrase the reason-phrase.
     */
    private HttpStatusCode(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Parses an {@link Integer} and tries to match the {@link HttpStatusCode}.
     *
     * @param code the status code of a {@link HttpStatusCode}.
     * @return the {@link HttpStatusCode} if found, null otherwise.
     */
    public static HttpStatusCode forCode(int code) {
        return intToValueMap.get(code);
    }

    /**
     * Returns the status code of this HTTP status.
     *
     * @return the status code of this HTTP status.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Returns the reason-phrase of this HTTP status.
     *
     * @return the reason-phrase of this HTTP status.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}