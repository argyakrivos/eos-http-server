package com.akrivos.eos.utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * An Enum singleton used for loading the mime-types from a resource file,
 * as well as matching mime-types to file extensions via the method
 * {@link #getMimeTypeFor(String)}.
 */
public enum MimeTypes {
    INSTANCE;

    private static final Logger logger = Logger.getLogger(MimeTypes.class);
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
    private final Map<String, String> map;

    /**
     * Initialises the singleton instance.
     */
    private MimeTypes() {
        map = new HashMap<String, String>();
        populateMimeTypes();
    }

    /**
     * Loading the mime-types from the resource file to the {@link Map}.
     */
    private void populateMimeTypes() {
        try {
            InputStream in = getClass().getResourceAsStream("/mime.types");
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                // # means comment, so skip it
                if (line.startsWith("#")) {
                    continue;
                }
                // match any number of \t characters
                String[] linePart = line.split("\\t+");
                if (linePart.length == 2) {
                    // there is a chance that many extensions share a
                    // mime-type, so split them and put all in the map.
                    String[] extPart = linePart[1].split(" ");
                    for (String ext : extPart) {
                        map.put(ext, linePart[0]);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            logger.error("Error while reading mime.types", e);
        }
    }

    /**
     * Extracts the extension of the filename given and tries to find a match
     * in the already populated {@link Map}. If there is no match, it tries to
     * guess it using {@link URLConnection#guessContentTypeFromName(String)}.
     * If all of them fail, it just returns the default mime-type.
     *
     * @param filename the absolute path to the file.
     * @return the mime-type of the given file.
     */
    public String getMimeTypeFor(String filename) {
        try {
            String ext = filename.substring(filename.lastIndexOf('.') + 1);
            String mimeType = map.get(ext);
            if (mimeType != null && !mimeType.isEmpty()) {
                return mimeType;
            }
            String guessed = URLConnection.guessContentTypeFromName(filename);
            if (guessed != null && !guessed.isEmpty()) {
                return guessed;
            }
        } catch (Exception e) {
            logger.error("Error while getting mime-type for " + filename, e);
        }
        return DEFAULT_MIME_TYPE;
    }
}