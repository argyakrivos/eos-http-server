package com.akrivos.eos.http;

import com.akrivos.eos.Handler;
import com.akrivos.eos.Server;
import com.akrivos.eos.http.constants.HttpResponseHeader;
import com.akrivos.eos.http.constants.HttpStatusCode;
import com.akrivos.eos.utils.MimeTypes;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An implementation of a {@link Handler} for file managing on an HTTP Server.
 */
public class FilesHandler implements Handler {
    private final String root;
    private Server server;

    /**
     * Creates a new FileHandler to start handling file requests.
     *
     * @param root the door directory.
     */
    public FilesHandler(String root) {
        if (root.startsWith("~")) {
            this.root = root.replaceFirst("~", System.getProperty("user.home"));
        } else {
            this.root = root;
        }
    }

    /**
     * Handles the connection by trying to create an {@link HttpRequest}
     * and check whether the request is about a file or a directory.
     * If the request is not valid, the appropriate {@link HttpException}
     * is thrown and an error page is generated and sent based on it.
     *
     * @param socket the client socket.
     * @return true if the request was handled successfully, false otherwise.
     * @throws Exception any exception that might occur.
     */
    @Override
    public boolean handle(Socket socket) throws Exception {
        HttpRequest request = null;
        try {
            request = new HttpRequest(socket.getInputStream());
            if (isRequestUriFile(request.getUri())) {
                sendFile(request, socket.getOutputStream());
            } else {
                sendDirectoryList(request, socket.getOutputStream());
            }
        } catch (HttpException e) {
            sendError(request, socket.getOutputStream(), e);
        }
        return true;
    }

    /**
     * @see Handler#getServer()
     */
    @Override
    public Server getServer() {
        return server;
    }

    /**
     * @see Handler#setServer(Server)
     */
    @Override
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * Checks if the requested file path exist and can be read. Then it
     * returns true or false, whether it is a normal file or a directory.
     *
     * @param uri the requested file path.
     * @return true if the uri is a normal file, false if it is a directory.
     * @throws HttpException {@link HttpStatusCode#NOT_FOUND} if the
     *                       file or directory does not exist, or
     *                       {@link HttpStatusCode#FORBIDDEN} if the
     *                       file or directory cannot be accessed.
     * @throws IOException   any IOException that might occur.
     */
    private boolean isRequestUriFile(String uri)
            throws HttpException, IOException {
        File file = new File(root, uri).getCanonicalFile();
        if (!file.exists()) {
            throw new HttpException(HttpStatusCode.NOT_FOUND);
        }
        if (!file.canRead()) {
            throw new HttpException(HttpStatusCode.FORBIDDEN);
        }
        return file.isFile();
    }

    private void sendFile(HttpRequest request, OutputStream out)
            throws Exception {
        File file = new File(root, request.getUri()).getCanonicalFile();
        HttpResponse response = new HttpResponse(request, out);
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(file));
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            byte[] buffer = new byte[4 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                if (bytesRead > 0) {
                    body.write(buffer, 0, bytesRead);
                }
            }
            response.setHeader(HttpResponseHeader.ContentType,
                    MimeTypes.INSTANCE.getMimeTypeFor(file.getCanonicalPath()));
            response.setBody(body.toByteArray());
            response.send();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void sendDirectoryList(HttpRequest request, OutputStream out)
            throws Exception {
        File dir = new File(root, request.getUri()).getCanonicalFile();
        File[] files = dir.listFiles();
        List<File> filesList = new ArrayList<File>(Arrays.asList(files));
        Collections.sort(filesList, new Comparator<File>() {
            @Override
            public int compare(File x, File y) {
                if (x.isDirectory() && !y.isDirectory()) {
                    return -1;
                }
                if (x.isDirectory() && y.isDirectory()) {
                    return 0;
                }
                if (!x.isDirectory() && y.isDirectory()) {
                    return 1;
                }
                return x.getName().compareTo(y.getName());
            }
        });

        // read html directory listing template from resources
        InputStream in = getClass().getResourceAsStream("/templates/listing.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder html = new StringBuilder();
        String line;
        // build the final html output by replacing the templated text
        while ((line = reader.readLine()) != null) {
            if (line.contains("${PATH}")) {
                line = line.replace("${PATH}", request.getUri());
            } else if (line.contains("${ITEM.NAME}")) {
                for (File f : filesList) {
                    SimpleDateFormat df = new SimpleDateFormat(
                            "yyyy-MMM-dd HH:mm:ss zzz");
                    boolean isDirectory = f.isDirectory();
                    String fileLine = line;
                    String fName = f.getName();
                    String itemSize = isDirectory
                            ? "--"
                            : readableSize(f.length(), true);
                    String itemType = isDirectory
                            ? "Directory"
                            : MimeTypes.INSTANCE.getMimeTypeFor(f.getCanonicalPath());
                    String itemLink = isDirectory ? f.getName() + "/" : fName;
                    String itemBs = isDirectory ? "/" : "";
                    String itemDate = df.format(new Date(f.lastModified()));
                    fileLine = fileLine.replace("${ITEM.LINK}", itemLink);
                    fileLine = fileLine.replace("${ITEM.NAME}", fName);
                    fileLine = fileLine.replace("${ITEM.BS}", itemBs);
                    fileLine = fileLine.replace("${ITEM.MODIFIED}", itemDate);
                    fileLine = fileLine.replace("${ITEM.SIZE}", itemSize);
                    fileLine = fileLine.replace("${ITEM.TYPE}", itemType);
                    html.append(fileLine + "\n");
                }
                continue;
            } else if (line.contains("${SERVER}")) {
                line = line.replace("${SERVER}", HttpServer.SERVER_NAME);
            }
            html.append(line + "\n");
        }
        // send directory listing response
        HttpResponse response = new HttpResponse(request, out);
        response.setHeader(HttpResponseHeader.ContentType,
                "text/html; charset=utf-8");
        response.setBody(html.toString());
        response.send();
    }

    /**
     * Generates the HTML error page based on the {@link HttpRequest} and
     * the {@link HttpException} and sends it as a {@link HttpResponse}.
     *
     * @param request the {@link HttpRequest}.
     * @param out     {@link OutputStream} from the client {@link Socket}.
     * @param e       the {@link HttpException}.
     * @throws Exception any exception that might occur.
     */
    private void sendError(HttpRequest request, OutputStream out, HttpException e)
            throws Exception {
        // read html error page template from resources
        InputStream in = getClass().getResourceAsStream("/templates/error.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder html = new StringBuilder();
        String line;
        // build the final html output by replacing the templated text
        while ((line = reader.readLine()) != null) {
            if (line.contains("${TITLE}")) {
                line = line.replace("${TITLE}",
                        String.format("%s - %s", e.getCode(), e.getMessage()));
            } else if (line.contains("${SERVER}")) {
                line = line.replace("${SERVER}", HttpServer.SERVER_NAME);
            }
            html.append(line + "\n");
        }
        // send error response
        HttpResponse response = new HttpResponse(request, out, e);
        response.setHeader(HttpResponseHeader.ContentType,
                "text/html; charset=utf-8");
        response.setBody(html.toString());
        response.send();
    }

    /**
     * Transforms a size from {@link long} into a readable size with
     * the appropriate unit (B / KB / MB / GB)
     * http://stackoverflow.com/questions/3758606/
     *
     * @param bytes the size in bytes.
     * @param si    use SI units or binary.
     * @return a readable representation of the size with units.
     */
    private String readableSize(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}