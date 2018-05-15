package org.zanata.magpie.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ShortStrings are meant for use in logging. They don't incur the cost of
 * shortening until toString() is called. This means they hold on to the entire
 * string, so don't bother keeping them around in memory for long.
 *
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class ShortString {

    static final int MAX_LENGTH = 100;
    private static final String ELLIPSIS = "…";

    @SuppressWarnings("unused")
    private ShortString() {
    }

    /**
     * Truncate a string to be no longer than MAX_LENGTH, using an ellipsis at
     * the end. If the string is already short enough, return the same string.
     * @param s String to shorten if required
     * @return a new String if shortening was required, otherwise s
     */
    public static String shorten(String s) {
        return shorten(s, MAX_LENGTH);
    }

    /**
     * Truncate a string to be no longer than maxLength, using an ellipsis at
     * the end. If the string is already short enough, return the same string.
     * @param s String to shorten if required
     * @param maxLength length to shorten string to
     * @return a new String if shortening was required, otherwise s
     */
    public static String shorten(String s, int maxLength) {
        if (s.length() <= maxLength) {
            return s;
        }
        return s.substring(0, maxLength - ELLIPSIS.length()) + ELLIPSIS;
    }

    /**
     * Truncate list of strings to be no longer than maxLength individually.
     */
    public static List<String> shorten(List<String> strings) {
        return shorten(strings, MAX_LENGTH);
    }

    /**
     * Truncate list of strings to be no longer than maxLength individually.
     */
    public static List<String> shorten(List<String> strings, int maxLength) {
        return strings.stream()
                .map(s -> shorten(s, maxLength))
                .collect(Collectors.toList());
    }

}
