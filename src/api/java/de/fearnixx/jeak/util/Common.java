package de.fearnixx.jeak.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

/**
 * Created by Life4YourGames on 29.06.16.
 */
public class Common {

    private Common() {
        // Hide public constructor
    }

    public static String stripVersion(String s) {
        s = s.replaceAll("(?!\\.)([a-zA-Z. ]*)", "");
        return s.isEmpty() ? "0" : s;
    }

    public static void copyStream(InputStream i, OutputStream o, Integer bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize == null ? 1024 : bufferSize];
        int bytesRead;
        while ((bytesRead = i.read(buffer)) != -1) {
            o.write(buffer, 0, bytesRead);
        }
    }
}
