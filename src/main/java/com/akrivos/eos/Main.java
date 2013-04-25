package com.akrivos.eos;

import com.akrivos.eos.config.Settings;
import org.apache.log4j.Logger;

/**
 * Entry point of EOS
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        // check if we are given a server configuration
        if (args.length > 0) {
            Settings.INSTANCE.loadSettings(args[0]);
        }

        // exit if the settings are not valid
        if (!Settings.INSTANCE.areValid()) {
            logger.error("Cannot start: server configuration is invalid.");
            System.exit(1);
        }

        // get validated settings
        String address = Settings.INSTANCE.getValueFor(Settings.SERVER_ADDRESS);
        int port = Settings.INSTANCE.getValueAsIntegerFor(Settings.SERVER_PORT);
        String root = Settings.INSTANCE.getValueFor(Settings.SERVER_ROOT);

        // create the connector
        Connector connector = new SocketConnector(10);
        connector.setAddress(address);
        connector.setPort(port);

        Connector[] connectors = new Connector[]{connector};

        // create the handler
        Handler handler = new FilesHandler(root);

        // create the server
        Server server = new HttpServer();
        server.setConnectors(connectors);
        server.setHandler(handler);

        // assign server to connector and handler
        connector.setServer(server);
        handler.setServer(server);

        // start the server
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Could not start the server on "
                    + address + ":" + port);
            System.exit(1);
        }
    }
}