package com.akrivos.eos.http;

import com.akrivos.eos.http.constants.HttpMethod;
import com.akrivos.eos.http.constants.HttpRequestHeader;
import com.akrivos.eos.http.constants.HttpStatusCode;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that holds every information that describes an HTTP request.
 * Includes headers, parameters, method, uri, http version, etc.
 */
public class HttpRequest {
    private static final Logger logger = Logger.getLogger(HttpRequest.class);
    private static final int MAX_URI_LENGTH = 4096;

    private final Map<HttpRequestHeader, String> headers;
    private final Map<String, String> parameters;
    private final BufferedReader reader;
    private HttpMethod method;
    private String uri;
    private float httpVersion;

    /**
     * Creates a new {@link HttpRequest} with an {@link InputStream} from
     * the client's {@link java.net.Socket} to start reading data.
     *
     * @param inputStream the {@link InputStream}.
     * @throws HttpException any {@link HttpException} that might occur
     *                       during the creation of an {@link HttpRequest}.
     */
    public HttpRequest(InputStream inputStream) throws HttpException {
        headers = new HashMap<HttpRequestHeader, String>();
        parameters = new HashMap<String, String>();
        reader = new BufferedReader(new InputStreamReader(inputStream));
        parseRequest();
    }

    /**
     * Returns the {@link HttpMethod}.
     *
     * @return the {@link HttpMethod}.
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Returns the Uri.
     *
     * @return the Uri.
     */
    public String getUri() {
        return uri;
    }

    /**
     * Returns the HTTP version.
     *
     * @return the HTTP version.
     */
    public float getHttpVersion() {
        return httpVersion;
    }

    /**
     * Returns the value of the given {@link HttpRequestHeader}.
     *
     * @param header the {@link HttpRequestHeader}.
     * @return the value of the {@link HttpRequestHeader} if found,
     *         null otherwise.
     */
    public String getHeader(HttpRequestHeader header) {
        return headers.get(header);
    }

    /**
     * Returns the value of the given parameter.
     *
     * @param parameter the parameter's name.
     * @return the value of the parameter if found, null otherwise.
     */
    public String getParameter(String parameter) {
        return parameters.get(parameter);
    }

    /**
     * Parses an HTTP request in three parts: a) decodes the request line,
     * b) decodes the headers, and c) decodes the parameters (if any).
     *
     * @throws HttpException any {@link HttpException} that might occur.
     */
    private void parseRequest() throws HttpException {
        decodeRequestLine();
        decodeHeaders();
        decodeParameters();
    }

    /**
     * Decodes the first line of the HTTP request (aka Request Line).
     * Request-Line = Method SP Request-URI SP HTTP-Version CRLF.
     * Also checks for the validity of the request, where the
     * appropriate {@link HttpException} is thrown, if it's invalid.
     *
     * @throws HttpException any {@link HttpException} that might occur.
     */
    private void decodeRequestLine() throws HttpException {
        // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
        String requestLine;
        try {
            requestLine = reader.readLine();
        } catch (IOException e) {
            throw new HttpException(HttpStatusCode.INTERNAL_SERVER_ERROR);
        }

        if (requestLine == null || requestLine.isEmpty()) {
            throw new HttpException(HttpStatusCode.BAD_REQUEST);
        }

        if (logger.isInfoEnabled()) {
            logger.info(String.format("Request: %s", requestLine));
        }

        String[] requestParts = requestLine.split(HttpServer.SP);

        // check if we have a valid request which comprises of the three
        // parts as described above (Request-Line)
        if (requestParts.length != 3) {
            throw new HttpException(HttpStatusCode.BAD_REQUEST);
        }

        // try to parse the HTTP method
        method = HttpMethod.forName(requestParts[0]);
        if (method == null
                || method == HttpMethod.PUT
                || method == HttpMethod.DELETE
                || method == HttpMethod.CONNECT
                || method == HttpMethod.TRACE) {
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
    }

    /**
     * Decodes the HTTP request headers and stores them in a {@link Map}.
     * Header-Field: Header Value.
     * It only stores supported headers by {@link HttpRequestHeader}.
     *
     * @throws HttpException any {@link HttpException} that might occur.
     */
    private void decodeHeaders() throws HttpException {
        try {
            String line = reader.readLine().trim();
            while (line != null && !line.isEmpty()) {
                // Header-Field: Header Value
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
    }

    /**
     * Decodes the HTTP request parameters and stores them in a {@link Map}.
     * name=John+Doe&age=25&...
     * Decoding parameters is only supported for POST and PUT, and for
     * application/x-www-form-urlencoded encoding.
     *
     * @throws HttpException any {@link HttpException} that might occur.
     */
    private void decodeParameters() throws HttpException {
        // check if it is a POST/PUT request, where we may have the
        // parameters right after the headers
        if (method != HttpMethod.POST || method != HttpMethod.PUT) {
            return;
        }

        String contentType = headers.get(HttpRequestHeader.ContentType);
        String contentLength = headers.get(HttpRequestHeader.ContentLength);

        // if there is Content-Type but no Content-Length
        if (contentType != null && (contentLength == null || contentLength.equals("0"))) {
            throw new HttpException(HttpStatusCode.LENGTH_REQUIRED);
        }

        // if there is no Content-Length header or it's set to zero,
        // there is no need to check for parameters
        if (contentLength == null || contentLength.equalsIgnoreCase("0")) {
            return;
        }

        // parse Content-Length to int
        int contentLengthValue;
        try {
            contentLengthValue = Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            throw new HttpException(HttpStatusCode.BAD_REQUEST);
        }

        // if there is Content-Length but no Content-Type,
        // then this is a bad request
        if (contentType == null) {
            throw new HttpException(HttpStatusCode.BAD_REQUEST);
        }

        // default encoding is utf-8
        String encoding = "UTF-8";
        // matches "charset=XXX" and we extract XXX
        Pattern charsetPattern = Pattern.compile("charset=(.+)",
                Pattern.CASE_INSENSITIVE);
        // get the encoding from the content-type
        Matcher charsetMatcher = charsetPattern.matcher(contentType);
        if (charsetMatcher.find()) {
            if (charsetMatcher.groupCount() == 1) {
                // make sure there is an actual value
                String val = charsetMatcher.group(1).trim();
                if (!val.isEmpty()) {
                    encoding = val;
                }
            }
        }

        if (contentType.contains("application/x-www-form-urlencoded")) {
            try {
                // read the parameters line using the content-length from header
                char[] buff = new char[contentLengthValue];
                reader.read(buff);
                String line = String.valueOf(buff);
                if (line != null && !line.isEmpty()) {
                    // name=John+Doe&age=25&...
                    String[] params = line.split("&");
                    for (String param : params) {
                        String[] paramPart = param.split("=", 2);
                        if (paramPart.length != 2) {
                            throw new HttpException(HttpStatusCode.BAD_REQUEST);
                        }
                        // separate key-value pairs and decode them
                        String pKey = URLDecoder.decode(paramPart[0], encoding);
                        String pVal = URLDecoder.decode(paramPart[1], encoding);
                        // add it to the parameters map
                        parameters.put(pKey, pVal);
                    }
                }
            } catch (IOException e) {
                throw new HttpException(HttpStatusCode.INTERNAL_SERVER_ERROR);
            }
        } else if (contentType.contains("multipart/form-data")) {
            // TODO: implement multipart/form-data decoding
            throw new HttpException(HttpStatusCode.NOT_IMPLEMENTED);
        } else {
            throw new HttpException(HttpStatusCode.BAD_REQUEST);
        }
    }
}