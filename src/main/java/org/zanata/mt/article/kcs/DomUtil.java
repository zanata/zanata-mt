package org.zanata.mt.article.kcs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

/**
 * Utility for KCS converter
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
final class DomUtil {
    public static final String ID_PREFIX = "ZanataMT";

    @SuppressWarnings("unused")
    private DomUtil() {
    }

    protected static String generateNodeId(String id) {
        return ID_PREFIX + "-" + id;
    }


    protected static Element generateNonTranslatableNode(String id) {
        Attributes attributes = new Attributes();
        attributes.put("id", generateNodeId(id));
        attributes.put("translate", "no");
        return new Element(Tag.valueOf("meta"), "", attributes);
    }

    /**
     * Get child elements of <pre> with parent <div> with class="code-raw"
     */
    protected static Elements getRawCodePreElements(Element element) {
        return element.select("div.code-raw > pre");
    }

    /**
     * Check if element if private notes
     *
     * section with id 'private-notes...'
     */
    protected static boolean isPrivateNotes(Element element) {
        return element.id().startsWith("private-notes");
    }

    /**
     * Parse html and return as Jsoup elements
     *
     * @param html - html to parse
     */
    protected static Elements parseAsElement(String html) {
        Document doc = Jsoup.parse(html);
        Element body = doc.body();
        return body != null && !body.children().isEmpty()
            ? body.children() : null;
    }

    protected static String extractBodyContentHTML(Document doc) {
        return doc.body() != null ? doc.body().html() : "";
    }
}
