package org.zanata.mt.util;

import java.security.MessageDigest;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Hex;
import org.zanata.mt.api.dto.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class HashUtil {
    public static final String SEPARATOR = "|";

    public static String generateHash(String string, LocaleId localeId)
        throws RuntimeException {
        try {
            String hashContent = string + SEPARATOR + localeId;
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
