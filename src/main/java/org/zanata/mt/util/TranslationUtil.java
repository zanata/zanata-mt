package org.zanata.mt.util;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;
import org.jsoup.nodes.Element;
import org.zanata.mt.api.dto.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class TranslationUtil {
    public static final String SEPARATOR = "|";
    public static String generateHash(String string, LocaleId localeId) {
        try {
            String hashContent = string + SEPARATOR + localeId;
            MessageDigest exc = MessageDigest.getInstance("MD5");
            exc.reset();
            return new String(
                    Hex.encodeHex(exc.digest(hashContent.getBytes("UTF-8"))));
        } catch (Exception var2) {
            throw new RuntimeException(var2);
        }
    }

    /**
     * Check if element is div with class "code-raw"
     * Used for KCS article translation
     */
    public static boolean isRawCodeParagraph(Element element) {
        return element.tagName().equals("div")
            && element.classNames().contains("code-raw");
    }

    /**
     * Check if element if private notes
     */
    public static boolean isPrivateNotes(Element element) {
        return element.id().startsWith("private-notes");
    }

    /**
     * Check if element is pre tag
     */
    public static boolean isPreElement(Element element) {
        return element.tagName().equals("pre");
    }
}
