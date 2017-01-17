package org.zanata.mt.article.kcs;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

/**
 * Utility for KCS converter
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
final class KCSUtil {
    public static final String ID_PREFIX = "ZanataMT";

    @SuppressWarnings("unused")
    private KCSUtil() {
    }

    protected static String generateCodeElementName(int sectionIndex,
            int codeElementsIndex) {
        return ID_PREFIX + "-"
                + String.valueOf(sectionIndex + "_" + codeElementsIndex);
    }

    protected static Element generateNonTranslatableNode(String name) {
        Attributes attributes = new Attributes();
        attributes.put("name", name);
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
}
