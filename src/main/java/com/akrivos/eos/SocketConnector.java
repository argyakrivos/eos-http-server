package com.akrivos.eos;

import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class SocketConnector implements Connector {
    private static final Logger logger = Logger.getLogger(SocketConnector.class);
    private final int receivers;
    private final Set<Connection> connections;
    private ServerSocket serverSocket;
    private Server server;
    private String address;
    private int port;

    public SocketConnector(int receivers) {
        this.receivers = receivers;
        connections = new HashSet<Connection>();
    }

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

    @Override
    public void stop() throws Exception {

    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    private synchronized void addConnection(Connection c) {
        connections.add(c);
    }

    private synchronized void removeConnection(Connection c) {
        connections.remove(c);
    }

    private class Connection implements Runnable {
        private final Socket socket;

        public Connection(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            addConnection(this);
            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Handling connection...");
                }
                server.handle(socket);
                if (logger.isInfoEnabled()) {
                    logger.info("Finished handling connection");
                }
            } catch (Exception e) {
                logger.error("Error while handling the connection");
            } finally {
                try {
                    close();
                } catch (Exception e) {
                    logger.error("Error while closing the connection");
                }
            }
        }

        public void close() throws Exception {
            if (socket != null) {
                socket.close();
                removeConnection(this);
            }
        }
    }
}