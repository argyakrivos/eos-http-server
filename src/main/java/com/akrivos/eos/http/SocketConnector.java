package com.akrivos.eos.http;

import com.akrivos.eos.Connector;
import com.akrivos.eos.Server;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * An implementation of a {@link com.akrivos.eos.Connector} that listens on a specified address
 * and port, with a specific number of receivers, accepting connections and
 * enqueuing them to the server's {@link com.akrivos.eos.ThreadPool}
 */
public class SocketConnector implements Connector {
    private static final Logger logger = Logger.getLogger(SocketConnector.class);

    private final int receivers;
    private final Set<Connection> connections;
    private ServerSocket serverSocket;
    private Server server;
    private String address;
    private int port;

    /**
     * Creates a new SocketConnector with the specified number of receivers.
     *
     * @param receivers the number of receivers.
     */
    public SocketConnector(int receivers) {
        this.receivers = receivers;
        connections = new HashSet<Connection>();
    }

    /**
     * Starts the {@link SocketConnector} by spawning the receivers, who wait until
     * a connection is accepted, and finally adding it to the {@link com.akrivos.eos.ThreadPool}
     * to be handled by the server's handler.
     *
     * @throws Exception any exception that might occur.
     */
    @Override
    public void start() throws Exception {
        serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(address, port));
        for (int i = 0; i < receivers; i++) {
            server.enqueueTask(new Runnable() {
                @Override
                public void run() {
                    if (logger.isInfoEnabled()) {
                        logger.info("Waiting for connection...");
                    }
                    try {
                        do {
                            // wait for connection
                            Socket socket = serverSocket.accept();

                            // disable Nagle's algorithm to decrease latency
                            // and increase performance.
                            socket.setTcpNoDelay(true);

                            // keep-alive for 60 seconds.
                            socket.setSoTimeout(60 * 1000);

                            // add to ThreadPool
                            if (logger.isInfoEnabled()) {
                                logger.info("Added connection to ThreadPool");
                            }
                            server.enqueueTask(new Connection(socket));
                        } while (!serverSocket.isClosed());
                    } catch (Exception e) {
                        logger.error("An error occurred while waiting for a request", e);
                    }
                }
            });
        }
    }

    /**
     * Stops the {@link SocketConnector} by closing all connections
     * and removing all references to them.
     *
     * @throws Exception any exception that might occur.
     */
    @Override
    public void stop() throws Exception {
        for (Connection c : connections) {
            c.close();
            removeConnection(c);
        }
    }

    /**
     * @see Connector#getServer()
     */
    @Override
    public Server getServer() {
        return server;
    }

    /**
     * @see Connector#setServer(Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @see Connector#getAddress()
     */
    @Override
    public String getAddress() {
        return address;
    }

    /**
     * @see Connector#setAddress(String)
     */
    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @see Connector#getPort()
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * @see Connector#setPort(int)
     */
    @Override
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Adds a {@link Connection} to our {@link Set}.
     *
     * @param c the connection.
     */
    private synchronized void addConnection(Connection c) {
        connections.add(c);
    }

    /**
     * Removed a {@link Connection} from our {@link Set}.
     *
     * @param c the connection.
     */
    private synchronized void removeConnection(Connection c) {
        connections.remove(c);
    }

    /**
     * Takes a request and sends it to the server for handling.
     */
    private class Connection implements Runnable {
        private final Socket socket;

        /**
         * Creates a new Connection.
         *
         * @param socket the connection's {@link Socket}.
         */
        public Connection(Socket socket) {
            this.socket = socket;
        }

        /**
         * Adds a connection and sends the request's {@link Socket}
         * to the server for handling.
         */
        @Override
        public void run() {
            addConnection(this);
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Handling connection...");
                }
                server.handle(socket);
                if (logger.isDebugEnabled()) {
                    logger.debug("Finished handling connection");
                }
            } catch (Exception e) {
                logger.error("Error while handling the connection: "
                        + e.getMessage());
            } finally {
                try {
                    close();
                } catch (Exception e) {
                    logger.error("Error while closing the connection: "
                            + e.getMessage());
                }
            }
        }

        /**
         * Closes the connection and removes it.
         *
         * @throws Exception any exception that might occur.
         */
        public void close() throws Exception {
            if (socket != null) {
                socket.close();
                removeConnection(this);
            }
        }
    }
}