package com.akrivos.eos;

import org.apache.log4j.Logger;

/**
 * Main HTTP server class
 */
public class HttpServer implements Runnable {
    private static final Logger log = Logger.getLogger(HttpServer.class);

    @Override
    public void run() {
        if (log.isInfoEnabled()) {
            log.info("Starting HTTP Server...");
        }
    }
}