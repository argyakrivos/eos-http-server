package com.akrivos.eos.http;

import com.akrivos.eos.*;

import java.net.Socket;

/**
 * An implementation of a {@link Server} for an HTTP Server.
 */
public class HttpServer implements Server {
    public static final String SERVER_NAME = "EOS/0.1";
    public static final String HTTP_VERSION = "HTTP/1.1";
    public static final String CRLF = "\r\n";
    public static final String SP = " ";

    private final ThreadPool threadPool;
    private Connector[] connectors;
    private Handler handler;

    /**
     * Creates a new HttpServer and its {@link ThreadPool}.
     */
    public HttpServer() {
        threadPool = new ServerThreadPool();
    }

    /**
     * @see Server#start()
     */
    @Override
    public void start() throws Exception {
        if (connectors != null) {
            for (Connector c : connectors) {
                c.start();
            }
        }
    }

    /**
     * @see Server#stop()
     */
    @Override
    public void stop() throws Exception {
        if (connectors != null) {
            for (Connector c : connectors) {
                c.stop();
            }
        }
    }

    /**
     * @see Server#getConnectors()
     */
    @Override
    public Connector[] getConnectors() {
        return connectors;
    }

    /**
     * @see Server#setConnectors(Connector[])
     */
    @Override
    public void setConnectors(Connector[] connectors) {
        this.connectors = connectors;
    }

    /**
     * @see Server#getHandler()
     */
    @Override
    public Handler getHandler() {
        return handler;
    }

    /**
     * @see Server#setHandler(Handler)
     */
    @Override
    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /**
     * @see Server#handle(Socket)
     */
    @Override
    public void handle(Socket socket) throws Exception {
        if (handler != null) {
            handler.handle(socket);
        }
    }

    /**
     * @see ThreadPool#enqueueTask(Runnable)
     */
    @Override
    public boolean enqueueTask(Runnable task) throws Exception {
        return threadPool.enqueueTask(task);
    }
}