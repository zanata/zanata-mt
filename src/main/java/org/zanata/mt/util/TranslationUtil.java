package org.zanata.mt.util;

import java.security.MessageDigest;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Hex;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.zanata.mt.api.dto.LocaleId;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class TranslationUtil {
    public static final String SEPARATOR = "|";
    public static final String ID_PREFIX = "ZanataMT";

    @SuppressWarnings("unused")
    private TranslationUtil() {
    }

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

    public static String generateNodeId(String id) {
        return ID_PREFIX + "-" + id;
    }


    public static Element generateNonTranslatableNode(String id) {
        Attributes attributes = new Attributes();
        attributes.put("id", generateNodeId(id));
        attributes.put("translate", "no");
        return new Element(Tag.valueOf("meta"), "", attributes);
    }

    /**
     * Get child elements of <pre> with parent <div> with class="code-raw"
     */
    public static Elements getRawCodePreElements(Element element) {
        return element.select("div.code-raw > pre");
    }

    /**
     * Check if element if private notes
     *
     * section with id 'private-notes...'
     */
    public static boolean isPrivateNotes(Element element) {
        return element.id().startsWith("private-notes");
    }

    /**
     * Parse html and return as Jsoup elements
     *
     * @param html - html to parse
     */
    public static Elements parseAsElement(String html) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        return body != null && !body.children().isEmpty()
            ? body.children() : null;
    }

    public static String extractBodyContentHTML(Document doc) {
        return doc.body() != null ? doc.body().html() : "";
    }
}
