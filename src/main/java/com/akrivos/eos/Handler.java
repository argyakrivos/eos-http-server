package com.akrivos.eos;

import java.net.Socket;

/**
 * Handler interface which is used by the {@link Server} in order
 * to deal with a task queued by the {@link Connector}.
 */
public interface Handler {
    /**
     * Handles a task with a given {@link Socket}.
     *
     * @param socket the client {@link Socket}.
     * @return true if the task was handled successfully, false otherwise.
     * @throws Exception any exception that might occur.
     */
    boolean handle(Socket socket) throws Exception;

    /**
     * Returns the {@link Server}.
     *
     * @return the {@link Server}.
     */
    Server getServer();

    /**
     * Sets the {@link Server}.
     *
     * @param server the {@link Server}.
     */
    void setServer(Server server);
}