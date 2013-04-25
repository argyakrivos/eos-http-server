package com.akrivos.eos;

/**
 * Connector interface which communicates with the server,
 * placing connection(s) in the {@link ThreadPool}.
 */
public interface Connector {
    /**
     * Starts the connector.
     *
     * @throws Exception any exception that might occur.
     */
    void start() throws Exception;

    /**
     * Stops the connector.
     *
     * @throws Exception any exception that might occur.
     */
    void stop() throws Exception;

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

    /**
     * Returns the server's address.
     *
     * @return the server's address.
     */
    String getAddress();

    /**
     * Sets the server's address.
     *
     * @param address the server's address.
     */
    void setAddress(String address);

    /**
     * Returns the server's port.
     *
     * @return the server's port.
     */
    int getPort();

    /**
     * Sets the server's port.
     *
     * @param port the server's port.
     */
    void setPort(int port);
}