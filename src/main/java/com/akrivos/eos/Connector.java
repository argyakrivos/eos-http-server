package com.akrivos.eos;

public interface Connector {
    void start() throws Exception;
    void stop() throws Exception;

    Server getServer();
    void setServer(Server server);

    String getAddress();
    void setAddress(String address);

    int getPort();
    void setPort(int port);
}