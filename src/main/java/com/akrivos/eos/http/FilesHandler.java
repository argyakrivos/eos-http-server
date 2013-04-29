package com.akrivos.eos.http;

import com.akrivos.eos.Handler;
import com.akrivos.eos.Server;
import com.akrivos.eos.http.constants.HttpResponseHeader;
import com.akrivos.eos.http.constants.HttpStatusCode;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * An implementation of a {@link Handler} for file managing on an HTTP Server.
 */
public class FilesHandler implements Handler {
    private static final Logger logger = Logger.getLogger(FilesHandler.class);

    private final String root;
    private Server server;

    /**
     * Creates a new FileHandler to start handling file requests.
     *
     * @param root the door directory.
     */
    public FilesHandler(String root) {
        this.root = root;
    }

    /**
     * Handles.
     *
     * @param socket the client socket.
     * @return true if the request was handled successfully, false otherwise.
     * @throws Exception any exception that might occur.
     */
    @Override
    public boolean handle(Socket socket) throws Exception {
        HttpRequest request;
        HttpResponse response;
        try {
            request = new HttpRequest(socket.getInputStream());
            if (logger.isInfoEnabled()) {
                logger.info(String.format("%s %s HTTP/%s", request.getMethod(),
                        request.getUri(), request.getHttpVersion()));
            }

            response = new HttpResponse(request, socket.getOutputStream());
        } catch (HttpException e) {
            // generate error html
            InputStream is = getClass().getResourceAsStream("/error.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder errorHtml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replace("${TITLE}", e.getCode() + " " + e.getMessage());
                errorHtml.append(line + "\n");
            }
            // send error response
            response = new HttpResponse(e, socket.getOutputStream());
            response.setHeader(HttpResponseHeader.ContentType,
                    "text/html; charset=utf-8");
            response.setBody(errorHtml.toString());
            response.send();
        }
        return true;
    }

    /**
     * @see Handler#getServer()
     */
    @Override
    public Server getServer() {
        return server;
    }

    /**
     * @see Handler#setServer(Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }
}