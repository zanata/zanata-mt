package org.zanata.mt.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zanata.mt.api.dto.TypeString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility class for Article
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ArticleUtil {
    static final String WRAPPER_ID = "ZanataMT-wrapper";

    @SuppressWarnings("unused")
    private ArticleUtil() {
    }

    /**
     * Generate a non-translatable html {@link TypeString}
     */
    public static String generateNonTranslatableHtml(String id) {
        return "<span id='" + generateNodeId(id) + "' translate='no'></span>";
    }

    public static String generateNodeId(String id) {
        return WRAPPER_ID + id;
    }

    /**
     * Check if element is non-translatable node
     *
     * section with id 'private-notes...'
     */
    public static boolean isNonTranslatableNode(String html) {
        Document document = Jsoup.parse(appendWrapper(html));
        Element content = getContentInWrapper(document);
        return content != null && content.hasAttr("translate") &&
                content.attr("translate").equals("no");
    }

    public static void replaceNodeById(String id, String originalHtml,
            TypeString translatedTypeString) {
        String nodeId = generateNodeId(id);

        Document document = Jsoup.parse(translatedTypeString.getValue());

        Element element = document.select("#" + nodeId).first();
        if (element != null) {
            element.replaceWith(generateNode(originalHtml));
        }
        translatedTypeString.setValue(document.body().html());
    }

    public static void replacePrivateNotes(TypeString typeString,
            String htmlToReplace) {
        Document document = Jsoup.parse(typeString.getValue());
        Element element = document.select("[id^='private-notes']").first();

        if (element != null) {
            Element replacingElement = generateNode(htmlToReplace);
            if (replacingElement != null) {
                element.replaceWith(replacingElement);
            }
        }
        if (!document.body().children().isEmpty()) {
            typeString.setValue(document.body().child(0).outerHtml());
        }
    }

    public static void replaceKCSCodeSection(TypeString typeString,
            String htmlToReplace) {
        Document document = Jsoup.parse(typeString.getValue());
        Element element = document.select("#code-raw").first();

        if (element != null) {
            Element replacingElement = generateNode(htmlToReplace);
            if (replacingElement != null) {
                element.replaceWith(replacingElement);
            }
        }
        if (!document.body().children().isEmpty()) {
            typeString.setValue(document.body().child(0).outerHtml());
        }
    }

    private static Element generateNode(String html) {
        return Jsoup.parse(html).body().child(0);
    }

    /**
     * Check if element contains KCS article private notes
     *
     * section with id 'private-notes...'
     */
    public static boolean containsPrivateNotes(String html) {
        Document document = Jsoup.parse(appendWrapper(html));
        Elements elements = document.select("[id^='private-notes']");
        return elements != null && !elements.isEmpty();
    }

    /**
     * Check if element contains KCS article code-raw section
     *
     * section with id 'private-notes...'
     */
    public static boolean containsKCSCodeSection(String html) {
        Document document = Jsoup.parse(appendWrapper(html));
        Elements elements = document.select("#code-raw");
        return elements != null && !elements.isEmpty();
    }

    /**
     * Wrap the html with a ZanataMT-wrapper for easy search
     */
    public static String appendWrapper(String html) {
        return "<div id='" + WRAPPER_ID + "'>" + html + "</div>";
    }

    /**
     * Get content in a ZanataMT-wrapper
     * @param element
     */
    public static
    @Nullable
    Element getContentInWrapper(@Nonnull Element element) {
        Element wrapper = element.getElementById(WRAPPER_ID);
        return wrapper.children().isEmpty() ? null : wrapper.children().first();
    }
}
