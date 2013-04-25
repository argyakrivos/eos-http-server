package com.akrivos.eos;

import java.net.Socket;

public interface Server extends ThreadPool {
    void start() throws Exception;
    void stop() throws Exception;

    Connector[] getConnectors();
    void setConnectors(Connector[] connectors);

    Handler getHandler();
    void setHandler(Handler handler);

    void handle(Socket socket) throws Exception;
}