package de.fearnixx.jeak.teamspeak.query;

/**
 * Created by MarkL4YG on 31.05.17.
 */
public class QueryEncoder {
    public static final char ESCAPE_CHAR = '\\';
    public static final char[] critChars = {
            '\\',
            '/',
            ' ',
            '|',
            // BELL NOT SUPPORTED ("\uD83D\uDD14"),
            '\u0008', // backspace
            '\f',
            '\n',
            '\r',
            '\t',
            '\u2B7F' // vertical tab
    };

    public static final String[] escapeSeqs = {
            "\\\\", // evaluates to \\
            "\\/",
            "\\s",
            "\\p",
            // BELL NOT SUPPORTED ("\\a"),
            "\\b",
            "\\f",
            "\\n",
            "\\r",
            "\\t",
            "\\v"
    };

    /**
     * Encode an ASCII String for the query connection
     * @param upTo only work on partial buffer - exclusive last index
     * @return A new buffer with critical characters replaced
     */
    public static char[] encodeBuffer(char[] origin, int upTo) {
        // Evaluate the new size first
        int pos;
        int innerPos;
        int replaced = 0;
        for (pos = 0; pos < upTo; pos++) {
            for (innerPos = 0; innerPos < critChars.length; innerPos++)
                if (origin[pos] == critChars[innerPos]) {
                    // Add an additional character
                    replaced++;
                }
        }
        char[] dest = new char[upTo + replaced];

        replaced = 0;
        int destPos;
        outer: for (pos = 0; pos < upTo; pos++) {
            destPos = pos + replaced;
            for (innerPos = 0; innerPos < critChars.length; innerPos++) {
                if (origin[pos] == critChars[innerPos]) {
                    // Replace critical character
                    dest[destPos] = escapeSeqs[innerPos].charAt(0);
                    dest[destPos+1] = escapeSeqs[innerPos].charAt(1);
                    replaced++;
                    continue outer;
                }
            }
            dest[destPos] = origin[pos];
        }
        return dest;
    }


    public static char[] encodeBuffer(char[] origin) {
        return encodeBuffer(origin, origin.length);
    }

    /**
     * Decode an ASCII String for the query connection
     * @param upTo only work on partial buffer - exclusive last index
     * @return A new buffer with escape characters replaced
     */
    public static char[] decodeBuffer(char[] origin, int upTo) {
        // Evaluate the new size first
        int pos;
        int innerPos;
        int size = upTo;
        for (pos = upTo-1; pos >= 0; pos--) {
            for (innerPos = 0; innerPos < escapeSeqs.length; innerPos++)
                if (origin[pos] == escapeSeqs[innerPos].charAt(0) && origin[pos+1] == escapeSeqs[innerPos].charAt(1))
                    // Subtract a character
                    size--;
        }
        char[] dest = new char[size];
        int replacements = 0;
        int origPos;
        outer: for (pos = 0; pos < dest.length; pos++) {
            // Each replacement causes the lengths to be 1 char off since we replace 2 chars with 1
            origPos = pos + replacements;
            for (innerPos = 0; innerPos < escapeSeqs.length; innerPos++)
                if (origin[origPos] == escapeSeqs[innerPos].charAt(0) && origin[origPos + 1] == escapeSeqs[innerPos].charAt(1)) {
                    // Replace escape characters
                    dest[pos] = critChars[innerPos];
                    replacements++;
                    // Abort normal copy
                    continue outer;
                }
            dest[pos] = origin[origPos];
        }
        return dest;
    }

    public static char[] decodeBuffer(char[] origin) {
        return decodeBuffer(origin, origin.length);
    }
}
