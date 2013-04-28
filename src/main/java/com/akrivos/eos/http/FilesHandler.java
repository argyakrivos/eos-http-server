package com.akrivos.eos.http;

import com.akrivos.eos.Handler;
import com.akrivos.eos.Server;
import org.apache.log4j.Logger;

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
        try {
            request = new HttpRequest(socket.getInputStream());
        } catch (HttpException e) {
            // show the appropriate error page
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