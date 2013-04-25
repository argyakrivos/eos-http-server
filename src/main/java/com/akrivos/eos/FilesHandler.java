package com.akrivos.eos;

import com.akrivos.eos.constants.HttpStatusCode;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.URLConnection;
import java.util.Vector;

public class FilesHandler implements Handler {
    private static final Logger logger = Logger.getLogger(FilesHandler.class);

    private final String root;
    private Server server;

    public FilesHandler(String root) {
        this.root = root;
    }

    @Override
    public boolean handle(Socket socket) throws Exception {
        // Request-Line = Method SP Request-URI SP HTTP-Version CRLF
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        String requestLine = in.readLine();
        String[] requestPart = requestLine.split(" ");

        System.out.println("Request received: " + requestLine);

        Vector<String> headers = new Vector<String>();
        while (in.ready()) {
            String input = in.readLine();
            System.out.println("Adding header: " + input);
            headers.add(input);
        }

        if (requestPart.length != 3) {
            socket.close();
            return false;
        }

        if (requestPart[0].equalsIgnoreCase("GET")) {
            String requestURI = requestPart[1];
            if (requestURI.startsWith("..") || requestURI.endsWith("..") || requestURI.contains("../")) {
                out.writeBytes("403 Forbidden" + "\r\n");
                out.writeBytes("Connection: close" + "\r\n");
            } else {
                if (requestURI.endsWith("/")) {
                    requestURI += "index.html";
                }
                String fileName = root + requestURI;
                File file = new File(fileName);
                if (file.exists() && file.isFile()) {
                    FileInputStream fin = new FileInputStream(fileName);
                    out.writeBytes("HTTP/1.1 200 OK" + "\r\n");
                    out.writeBytes("Content-Type: " + URLConnection.guessContentTypeFromName(fileName) + "\r\n");
                    out.writeBytes("Content-Length: " + fin.available() + "\r\n");
                    out.writeBytes("Connection: close" + "\r\n");
                    out.writeBytes("\r\n");
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fin.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    fin.close();
                } else {
                    out.writeBytes("HTTP/1.1 404 Not Found" + "\r\n");
                    out.writeBytes("Connection: close" + "\r\n");
                }
            }
        }

        return true;
    }

    /**
     * @see com.akrivos.eos.Handler#getServer()
     */
    @Override
    public Server getServer() {
        return server;
    }

    /**
     * @see com.akrivos.eos.Handler#setServer(Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }
}