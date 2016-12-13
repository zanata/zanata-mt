package org.zanata.mt.util;

import java.security.MessageDigest;

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

    public static Element getNonTranslatableNode(String id) {
        Attributes attributes = new Attributes();
        attributes.put("id", id);
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
        Element body = doc.select("body").isEmpty() ? null
            : doc.select("body").first();
        return body != null && !body.children().isEmpty()
            ? body.children() : null;
    }

    public static String getBodyHTMLContent(Document doc) {
        Element body = doc.select("body").isEmpty() ? null
            : doc.select("body").first();
        return body != null ? body.html() : "";
    }
}
