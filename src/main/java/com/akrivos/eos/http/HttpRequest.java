package com.akrivos.eos.http;

import com.akrivos.eos.http.constants.HttpMethod;
import com.akrivos.eos.http.constants.HttpRequestHeader;
import com.akrivos.eos.http.constants.HttpStatusCode;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    public static final int MAX_URI_LENGTH = 4096;

    private final InputStream inputStream;
    private final Map<HttpRequestHeader, String> headers;
    private final Map<String, String> parameters;
    private final BufferedReader reader;
    private HttpMethod method;
    private String uri;
    private float httpVersion;

    public HttpRequest(InputStream inputStream) throws HttpException {
        this.inputStream = inputStream;
        headers = new HashMap<HttpRequestHeader, String>();
        parameters = new HashMap<String, String>();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        parseRequest();
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public float getHttpVersion() {
        return httpVersion;
    }

    public String getHeader(HttpRequestHeader header) {
        return headers.get(header);
    }

    public String getParameter(String parameter) {
        return parameters.get(parameter);
    }

    private void parseRequest() throws HttpException {
        // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
        String requestLine;
        try {
            requestLine = reader.readLine();
        } catch (IOException e) {
            throw new HttpException(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
        String[] requestParts = requestLine.split(HttpServer.SP);

        // check if we have a valid request which comprises of the three
        // parts as described above (Request-Line)
        if (requestParts.length != 3) {
            throw new HttpException(HttpStatusCode.BAD_REQUEST);
        }

        // try to parse the HTTP method
        method = HttpMethod.forName(requestParts[0]);
        if (method == null) {
            throw new HttpException(HttpStatusCode.NOT_IMPLEMENTED);
        }

        // decode the URI using the URLDecoder
        try {
            uri = URLDecoder.decode(requestParts[1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new HttpException(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
        if (uri.startsWith("../") || uri.endsWith("..")) {
            throw new HttpException(HttpStatusCode.FORBIDDEN);
        }

        // maximum number of characters in URI is 4096
        if (uri.length() > MAX_URI_LENGTH) {
            throw new HttpException(HttpStatusCode.REQ_TOO_LONG);
        }

        // get the HTTP version
        if (requestParts[2].equalsIgnoreCase("HTTP/1.1")) {
            httpVersion = 1.1f;
        } else {
            httpVersion = 1f;
        }

        // parsing headers
        try {
            String line = reader.readLine().trim();
            while (line != null && !line.isEmpty()) {
                // Header Field : Header Value
                String[] headerPart = line.split(":", 2);
                if (headerPart.length != 2) {
                    throw new HttpException(HttpStatusCode.BAD_REQUEST);
                }
                // check if we support this header and then add it to the headers map
                HttpRequestHeader header = HttpRequestHeader.forName(headerPart[0].trim());
                if (header != null) {
                    headers.put(header, headerPart[1].trim());
                }
                line = reader.readLine().trim();
            }
        } catch (IOException e) {
            throw new HttpException(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }

        // check if it is a POST request, where we may have the
        // parameters right after the headers
        if (method == HttpMethod.POST) {
            String contentType = headers.get(HttpRequestHeader.ContentType);
            if (contentType.equalsIgnoreCase("application/x-www-form-urlencoded")) {
                try {
                    String line = reader.readLine().trim();
                    if (line != null && !line.isEmpty()) {
                        // name=John+Doe&age=25&...
                        String[] params = line.split("&");
                        for (String param : params) {
                            String[] paramPart = param.split("=", 2);
                            if (paramPart.length != 2) {
                                throw new HttpException(HttpStatusCode.BAD_REQUEST);
                            }
                            // separate key-value pairs and decode them
                            String pKey = URLDecoder.decode(paramPart[0], "UTF-8");
                            String pVal = URLDecoder.decode(paramPart[1], "UTF-8");
                            // add it to the parameters map
                            parameters.put(pKey, pVal);
                        }
                    }
                } catch (IOException e) {
                    throw new HttpException(HttpStatusCode.INTERNAL_SERVER_ERROR);
                }
            } else if (contentType.equalsIgnoreCase("multipart/form-data")) {
                throw new HttpException(HttpStatusCode.NOT_IMPLEMENTED);
            } else {
                throw new HttpException(HttpStatusCode.BAD_REQUEST);
            }
        }
    }
}