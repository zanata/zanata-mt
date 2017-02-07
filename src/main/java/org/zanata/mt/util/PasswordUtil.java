package org.zanata.mt.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * Helper class for Password
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class PasswordUtil {
    /**
     * Used building output as Hex
     */
    private static final char[] DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a',
            'b', 'c', 'd', 'e', 'f' };

    public static String createSaltedApiKey(String username) {
        try {
            byte[] salt = new byte[16];
            SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] name = username.getBytes("UTF-8");

            // add salt
            byte[] salted = new byte[name.length + salt.length];
            System.arraycopy(name, 0, salted, 0, name.length);
            System.arraycopy(salt, 0, salted, name.length, salt.length);

            // generate md5 digest
            md5.reset();
            byte[] digest = md5.digest(salted);

            return new String(PasswordUtil.encodeHex(digest));
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    /**
     * <b>copied from org.jboss.seam.util.Hex#encodeHex(byte[])</b>
     * Converts an array of bytes into an array of characters representing the
     * hexidecimal values of each byte in order. The returned array will be
     * double the length of the passed array, as it takes two characters to
     * represent any given byte.
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A char[] containing hexidecimal characters
     */
    public static char[] encodeHex(byte[] data) {

        int l = data.length;

        char[] out = new char[l << 1];

        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS[0x0F & data[i]];
        }
        return out;
    }
}
