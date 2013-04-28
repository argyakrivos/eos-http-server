package com.akrivos.eos.http;

import com.akrivos.eos.http.constants.HttpStatusCode;

/**
 * An HTTP flavour of {@link Exception}, with an error code and a message,
 * describing an exception during an {@link HttpRequest} parsing.
 */
public class HttpException extends Exception {
    private final int code;

    /**
     * Creates a new {@link HttpException} with the specified code and message.
     *
     * @param code    the HTTP error code.
     * @param message the HTTP error message.
     */
    public HttpException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Creates a new {@link HttpException} from an {@link HttpStatusCode},
     * parsing its status code and reason-phrase.
     *
     * @param statusCode the {@link HttpStatusCode}.
     */
    public HttpException(HttpStatusCode statusCode) {
        super(statusCode.getReasonPhrase());
        code = statusCode.getStatusCode();
    }

    /**
     * Returns the HTTP error code.
     *
     * @return the HTTP error code.
     */
    public int getCode() {
        return code;
    }
}