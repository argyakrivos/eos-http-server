package com.akrivos.eos;

import java.net.Socket;

/**
 * Server interface which extends {@link ThreadPool}.
 * A server has a {@link ThreadPool}, {@link Connector}(s) and a {@link Handler}.
 * The {@link Connector}(s) accepts connection(s) and puts them into the
 * {@link ThreadPool}. Then, they get picked-up by the {@link Handler} and executed.
 */
public interface Server extends ThreadPool {
    /**
     * Starts the server.
     *
     * @throws Exception any exception that might occur.
     */
    void start() throws Exception;

    /**
     * Stops the server.
     *
     * @throws Exception any exception that might occur.
     */
    void stop() throws Exception;

    /**
     * Returns the array field of {@link Connector}.
     *
     * @return the array of {@link Connector}s.
     */
    Connector[] getConnectors();

    /**
     * Sets the array of {@link Connector}.
     *
     * @param connectors the array of {@link Connector}.
     */
    void setConnectors(Connector[] connectors);

    /**
     * Returns the {@link Handler}.
     *
     * @return the {@link Handler}.
     */
    Handler getHandler();

    /**
     * Sets the {@link Handler}.
     *
     * @param handler the {@link Handler}.
     */
    void setHandler(Handler handler);

    /**
     * Handles an accepted request from the {@link ThreadPool}.
     *
     * @param socket the client socket coming from the {@link java.net.ServerSocket#accept()}.
     * @throws Exception any exceptions the might occur.
     */
    void handle(Socket socket) throws Exception;
}