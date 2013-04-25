package com.akrivos.eos;

import java.net.Socket;

/**
 * Main HTTP server class.
 */
public class HttpServer implements Server {
    public static final String HTTP_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    public static final String SP = " ";

    private final ThreadPool threadPool;
    private Connector[] connectors;
    private Handler handler;

    public HttpServer() {
        threadPool = new ServerThreadPool();
    }

    @Override
    public void start() throws Exception {
        if (connectors != null) {
            for (Connector c : connectors) {
                c.start();
            }
        }
    }

    @Override
    public void stop() throws Exception {
        if (connectors != null) {
            for (Connector c : connectors) {
                c.stop();
            }
        }
    }

    @Override
    public Connector[] getConnectors() {
        return connectors;
    }

    @Override
    public void setConnectors(Connector[] connectors) {
        this.connectors = connectors;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(Socket socket) throws Exception {
        if (handler != null)
            handler.handle(socket);
    }

    @Override
    public boolean enqueueTask(Runnable task) throws Exception {
        return threadPool.enqueueTask(task);
    }
}