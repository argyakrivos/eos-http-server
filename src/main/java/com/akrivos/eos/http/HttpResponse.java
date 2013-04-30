package com.akrivos.eos.http;

import com.akrivos.eos.http.constants.HttpMethod;
import com.akrivos.eos.http.constants.HttpResponseHeader;
import com.akrivos.eos.http.constants.HttpStatusCode;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

public class HttpResponse {
    private final Map<HttpResponseHeader, String> headers;
    private final BufferedWriter writer;
    private HttpStatusCode statusCode;
    private String body;
    private boolean wasHeadRequest;

    public HttpResponse(HttpRequest request, OutputStream out) {
        headers = new HashMap<HttpResponseHeader, String>();
        writer = new BufferedWriter(new OutputStreamWriter(out));
        wasHeadRequest = (request != null && request.getMethod() == HttpMethod.HEAD);
    }

    public HttpResponse(HttpRequest request, OutputStream out, HttpException e) {
        headers = new HashMap<HttpResponseHeader, String>();
        writer = new BufferedWriter(new OutputStreamWriter(out));
        wasHeadRequest = (request != null && request.getMethod() == HttpMethod.HEAD);
        setStatusCode(HttpStatusCode.forCode(e.getCode()));
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getHeader(HttpResponseHeader header) {
        return headers.get(header);
    }

    public void setHeader(HttpResponseHeader header, String value) {
        headers.put(header, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void send() throws Exception {
        // RFC 1123 date format
        SimpleDateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        setHeader(HttpResponseHeader.Date, df.format(new Date()));
        setHeader(HttpResponseHeader.ContentLength, String.valueOf(body.length()));
        //setHeader(HttpResponseHeader.Connection, "close");

        writeStatusLine();
        writeHeaders();
        if (!wasHeadRequest) {
            writeBody();
        }
    }

    private void writeStatusLine() throws Exception {
        // Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
        writer.write(String.format("%s%s%s%s%s%s",
                HttpServer.HTTP_VERSION,
                HttpServer.SP,
                statusCode.getStatusCode(),
                HttpServer.SP,
                statusCode.getReasonPhrase(),
                HttpServer.CRLF));
    }

    private void writeHeaders() throws Exception {
        for (Entry<HttpResponseHeader, String> header : headers.entrySet()) {
            String headerStr = String.format("%s: %s%s",
                    header.getKey().getName(),
                    header.getValue(), HttpServer.CRLF);
            writer.write(headerStr);
        }
    }

    private void writeBody() throws Exception {
        // line separating body from headers
        writer.write(HttpServer.CRLF);
        // write body
        writer.write(body);
        writer.flush();
    }
}