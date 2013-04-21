package com.akrivos.eos;

/**
 * Entry point of EOS
 */
public class Main {
    public static void main(String[] args) {
        HttpServer server;

        if (args.length > 0) {
            server = new HttpServer(args[0]);
        } else {
            server = new HttpServer();
        }

        Thread serverThread = new Thread(server);
        serverThread.setName("Server");
        serverThread.start();
    }
}