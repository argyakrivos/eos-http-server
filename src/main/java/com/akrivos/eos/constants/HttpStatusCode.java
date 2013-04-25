package com.akrivos.eos.constants;

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

    private final int statusCode;
    private final String reasonPhrase;

    private HttpStatusCode(int statusCode, String reasonPhrase) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public String getFullStatus() {
        return String.format("%d %s", statusCode, reasonPhrase);
    }
}