package com.akrivos.eos;

import com.akrivos.eos.config.Settings;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

/**
 * Main HTTP server class.
 */
public class HttpServer implements Runnable {
    private static final Logger log = Logger.getLogger(HttpServer.class);
    private ServerSocket serverSocket = null;
    private String address;
    private int port;

    /**
     * Initialises a new instance of the HttpServer class,
     * with the default properties file.
     */
    public HttpServer() { }

    /**
     * Initialises a new instance of the HttpServer class,
     * with the given properties file.
     *
     * @param propertiesFile the properties file.
     */
    public HttpServer(String propertiesFile) {
        Settings.INSTANCE.loadSettings(propertiesFile);
    }

    @Override
    public void run() {
        try {
            startServer();
            do {
                Socket socket = serverSocket.accept();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                String line;
                while ((line = in.readLine()) != null) {
                    log.info(line);
                }

            } while (!serverSocket.isClosed());
        } catch (IOException e) {
            log.error("Error while dealing with a request", e);
        }
    }

    /**
     * Checks if the server has valid configuration and then tries to start
     * the server. If the configuration is invalid or the server cannot be
     * started, it terminates the application.
     */
    private void startServer() {
        if (!hasValidConfiguration()) {
            log.error("Cannot start: server configuration is invalid.");
            System.exit(1);
        }

        address = Settings.INSTANCE.getValueFor(Settings.SERVER_ADDRESS);
        port = Settings.INSTANCE.getValueAsIntegerFor(Settings.SERVER_PORT);

        if (log.isInfoEnabled()) {
            log.info("Starting server...");
        }

        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(address, port));
            if (log.isInfoEnabled()) {
                log.info("Listening on " + address + ":" + port);
            }
        } catch (IOException ex) {
            log.error("Could not start the server on "
                    + address + ":" + port);
            System.exit(1);
        }
    }

    /**
     * Checks if every setting in server configuration is valid.
     *
     * @return true if all settings are valid; false otherwise.
     */
    private boolean hasValidConfiguration() {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Validating server configuration...");
            }

            String address = Settings.INSTANCE.getValueFor(Settings.SERVER_ADDRESS);
            Pattern ipPattern = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
            if (!ipPattern.matcher(address).matches()) {
                log.error("Error in server configuration: The server address "
                        + "is not a valid ip address (" + address + ")");
                return false;
            }

            int port = Settings.INSTANCE.getValueAsIntegerFor(Settings.SERVER_PORT);
            if (port < 1 || port > 65535) {
                log.error("Error in server configuration: The server port "
                        + "is out of range 1-65535 (" + port + ")");
                return false;
            }

            String root = Settings.INSTANCE.getValueFor(Settings.SERVER_ROOT);
            root = root.replace("~", System.getProperty("user.home"));
            File rootDirectory = new File(root);
            if (!rootDirectory.getCanonicalFile().isDirectory()) {
                log.error("Error in server configuration: The server root "
                        + "address is not a valid directory (" + root + ")");
                return false;
            }

            if (log.isTraceEnabled()) {
                log.trace("Server configuration validated successfully");
            }
            return true;
        } catch (Exception ex) {
            log.error("Error while validating configuration: "
                    + ex.getMessage());
            return false;
        }
    }
}