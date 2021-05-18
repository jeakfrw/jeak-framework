package de.fearnixx.jeak.service.http;

import java.util.regex.Pattern;

public class ControllerUtil {

    private static final Pattern startWithPattern = Pattern.compile("^/*(.+)$");
    private static final Pattern endWithPattern = Pattern.compile("^(.+?)/*$");

    public static String joinWithSlash(String a, String b) {
        final var first = a != null && !a.isBlank() ? endWithPattern.matcher(a).group(1) : "";
        final var last = b != null && !b.isBlank() ? startWithPattern.matcher(b).group(1) : "";
        return first + "/" + last;
    }
}
