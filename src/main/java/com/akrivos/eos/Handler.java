package com.akrivos.eos;

import java.net.Socket;

public interface Handler {
    boolean handle(Socket socket) throws Exception;

    void setServer(Server server);
    Server getServer();
}