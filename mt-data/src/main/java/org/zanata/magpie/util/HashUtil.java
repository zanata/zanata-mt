package org.zanata.magpie.util;

import java.security.MessageDigest;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Hex;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class HashUtil {
    @SuppressWarnings("unused")
    private HashUtil() {
    }

    public static String generateHash(String hashContent)
        throws RuntimeException {
        try {
            MessageDigest exc = MessageDigest.getInstance("MD5");
            exc.reset();
            return new String(
                Hex.encodeHex(
                    exc.digest(
                        hashContent.getBytes(CharEncoding.UTF_8))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
