package org.zanata.mt.article.kcs;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.zanata.mt.exception.ZanataMTException;

/**
 * Utility for KCS converter
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
final class KCSUtil {
    static final String ID_PREFIX = "ZanataMT";

    @SuppressWarnings("unused")
    private KCSUtil() {
    }

    static String generateCodeElementName(int codeElementsIndex) {
        return ID_PREFIX + "-" + String.valueOf(codeElementsIndex);
    }

    static Element generateNonTranslatableNode(String name) {
        Attributes attributes = new Attributes();
        attributes.put("name", name);
        attributes.put("translate", "no");
        return new Element(Tag.valueOf("meta"), "", attributes);
    }

    /**
     * Get child elements of <pre> with parent <div> with class="code-raw"
     */
    static Elements getRawCodePreElements(Element element) {
        return element.select("div.code-raw > pre");
    }

    /**
     * Check if element if private notes
     *
     * section with id 'private-notes...'
     */
    static boolean isPrivateNotes(Element element) {
        return element.id().startsWith("private-notes");
    }

    /**
     * Return element with 'header' tag
     */
    static @Nullable Element getHeader(Document document) {
        Elements elements = document.getElementsByTag("header");
        return elements.isEmpty() ? null : elements.first();
    }

    /**
     * Insert base64Image attribution after <header> tag
     */
    static void insertAttribution(@Nonnull Document document,
            @Nonnull String html) throws ZanataMTException {
        Element header = getHeader(document);
        if (header != null) {
            header.after(html);
        } else {
            throw new ZanataMTException(
                    "Unable to insert attribution after <header>");
        }
    }
}
