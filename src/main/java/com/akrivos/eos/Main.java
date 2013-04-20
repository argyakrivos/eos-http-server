package com.akrivos.eos;

/**
 * Entry point of EOS
 */
public class Main {
    public static void main(String[] args) {
        HttpServer server = new HttpServer();
        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }
}