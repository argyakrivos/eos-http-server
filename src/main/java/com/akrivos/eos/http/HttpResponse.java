package com.akrivos.eos.http;

import com.akrivos.eos.http.constants.HttpResponseHeader;
import com.akrivos.eos.http.constants.HttpStatusCode;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A class that holds every information that describes an HTTP response.
 * Includes status code, headers and body. It also provides a method
 * to send the response over an {@link OutputStream}.
 */
public class HttpResponse {
    private final Map<HttpResponseHeader, String> headers;
    private final DataOutputStream writer;
    private final SimpleDateFormat df;
    private HttpStatusCode statusCode;

    /**
     * Creates a new {@link HttpResponse} with an {@link HttpRequest} and
     * an {@link OutputStream} from the client's {@link java.net.Socket},
     * to send data to.
     *
     * @param request the {@link HttpRequest} to get information.
     * @param out     the {@link OutputStream} to send data to.
     */
    public HttpResponse(HttpRequest request, OutputStream out) {
        headers = new HashMap<HttpResponseHeader, String>();
        writer = new DataOutputStream(new BufferedOutputStream(out));

        // RFC 1123 date format
        df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Returns the {@link HttpStatusCode}.
     *
     * @return the {@link HttpStatusCode}.
     */
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the {@link HttpStatusCode} to the given value and sends
     * the status line to the client's {@link java.net.Socket}.
     * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF.
     *
     * @param statusCode the {@link HttpStatusCode}.
     * @throws Exception any exception that might occur.
     */
    public void writeStatusLine(HttpStatusCode statusCode) throws Exception {
        this.statusCode = statusCode;
        // Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
        writer.writeBytes(String.format("%s%s%s%s%s%s",
                HttpServer.HTTP_VERSION,
                HttpServer.SP,
                statusCode.getStatusCode(),
                HttpServer.SP,
                statusCode.getReasonPhrase(),
                HttpServer.CRLF));
    }

    /**
     * Returns the value of the given {@link HttpResponseHeader}.
     *
     * @param header the {@link HttpResponseHeader}.
     * @return the value of the {@link HttpResponseHeader} if found,
     *         null otherwise.
     */
    public String getHeader(HttpResponseHeader header) {
        return headers.get(header);
    }

    /**
     * Adds the {@link HttpResponseHeader} to the {@link Map} and sends
     * that header value to the client's {@link java.net.Socket}.
     *
     * @param header the {@link HttpResponseHeader}.
     * @param value  the {@link String} value.
     * @throws Exception any exception that might occur.
     */
    public void writeHeader(HttpResponseHeader header, String value) throws Exception {
        headers.put(header, value);
        String headerStr = String.format("%s: %s%s",
                header.getName(), value, HttpServer.CRLF);
        writer.writeBytes(headerStr);
        writer.flush();
    }

    /**
     * Writes the Last-Modified header using the RFC 1123 format.
     *
     * @param date the last modified {@link Date}.
     * @throws Exception any exception that might occur.
     */
    public void writeLastModified(Date date) throws Exception {
        writeHeader(HttpResponseHeader.LastModified, df.format(date));
    }

    /**
     * Writes the final {@link HttpResponseHeader}s, ending the headers section.
     *
     * @throws Exception any exception that might occur.
     */
    public void writeFinalHeaders() throws Exception {
        writeHeader(HttpResponseHeader.Date, df.format(new Date()));
        writeHeader(HttpResponseHeader.Server, HttpServer.SERVER_NAME);
        writer.writeBytes(HttpServer.CRLF);
        writer.flush();
    }

    /**
     * Writes the body data from a {@link String}.
     *
     * @param body the body data in {@link String}.
     * @throws Exception any exception that might occur.
     */
    public void writeBody(String body) throws IOException {
        writer.writeBytes(body);
        writer.flush();
    }

    /**
     * Writes the body data from a {@link Byte} array,
     * given a specific offset and length
     *
     * @param buffer the data.
     * @param offset the starting data offset.
     * @param length the length of bytes to write.
     * @throws IOException any exception that might occur.
     */
    public void writeBody(byte[] buffer, int offset, int length) throws IOException {
        writer.write(buffer, offset, length);
        writer.flush();
    }
}