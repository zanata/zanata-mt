package org.zanata.mt.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.zanata.mt.model.TranslatableHTMLNode;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for Article
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ArticleUtil {
    private final static String ID_PREFIX = "ZNTA";
    private final static String WRAPPER_ID = "WRAPPER";

    // HTML tag that will not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_NODE = ImmutableList
            .of("script", "#text", "code", "col", "colgroup", "embed", "em",
                    "#comment", "image", "map", "media", "meta", "source",
                    "xml", "pre");

    // Node with attribute that will not be translated
    private final static ImmutableMap<String, String> NON_TRANSLATABLE_ATTRIBUTE =
            ImmutableMap.of("translate", "no");

    // Node with css class that wil not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_CLASS =
            ImmutableList.of("notranslate");

    // Node with id prefix that will not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_ID =
            ImmutableList.of("private-notes");

    @SuppressWarnings("unused")
    private ArticleUtil() {
    }

    /**
     * Replace non-translatable node with placeholder
     *
     * @param index - index of the node, used to generate unique id for placeholder
     * @param html - html string
     */
    public static TranslatableHTMLNode replaceNonTranslatableNode(int index,
            String html) {
        Element document = wrapHTML(html);
        Map<String, Node> placeholderIdMap = new HashMap<>();

        int counter = 0;
        for (Map.Entry<String, String> entry : NON_TRANSLATABLE_ATTRIBUTE
                .entrySet()) {
            for (Element element : document
                    .getElementsByAttributeValueContaining(
                            entry.getKey(), entry.getValue())) {
                replaceNodeWithPlaceholder(placeholderIdMap, index,
                        counter, element);
                counter++;
            }
        }
        for (String tag : NON_TRANSLATABLE_NODE) {
            for (Element element : document.getElementsByTag(tag)) {
                replaceNodeWithPlaceholder(placeholderIdMap, index,
                        counter, element);
                counter++;
            }
        }
        for (String cssClass : NON_TRANSLATABLE_CLASS) {
            for (Element element : document.getElementsByClass(cssClass)) {
                replaceNodeWithPlaceholder(placeholderIdMap, index,
                        counter, element);
                counter++;
            }
        }
        for (String id : NON_TRANSLATABLE_ID) {
            String cssQuery = "[id^='" + id + "']";
            for (Element element : document.select(cssQuery)) {
                replaceNodeWithPlaceholder(placeholderIdMap, index,
                        counter, element);
                counter++;
            }
        }
        return new TranslatableHTMLNode(unwrapAsNode(document),
                placeholderIdMap);
    }

    /**
     * Generate an unique id with prefix and index
     */
    public static String generatePlaceholderId(int prefix, int index) {
        return ID_PREFIX + "-" + prefix + "-" + index;
    }

    /**
     * Generate a non-translatable element
     */
    public static Element generatePlaceholderNode(@NotNull String id) {
        Attributes attrs = new Attributes();
        attrs.put("id", id);
        attrs.put("translate", "no");
        return new Element(Tag.valueOf("var"), "", attrs);
    }

    /**
     * Wrap given html around ZANATA-MT wrapper for easy extraction
     *
     * IMPORTANT: This assumes the html is wrap in single html node
     */
    public static Element wrapHTML(String html) {
        String wrapHTML = "<div id='" + getWrapperId() + "'>" + html + "</div>";
        Document doc = Jsoup.parse(wrapHTML, "", Parser.xmlParser());
        doc.outputSettings().indentAmount(0).prettyPrint(false);
        return doc;
    }

    /**
     * Unwrap wrapped element inside ZANATA-MT wrapper. Only returns the
     * first child in the wrapper or the first text node.
     *
     * IMPORTANT: This assumes the html is wrap in single html node
     */
    public static @Nullable Node unwrapAsNode(@NotNull Element element) {
        Element wrapper = element.select("#" + getWrapperId()).first();
        if (wrapper != null) {
            if (!wrapper.children().isEmpty()) {
                return wrapper.children().first();
            } else if (!wrapper.textNodes().isEmpty()) {
                return wrapper.textNodes().get(0);
            }
        }
        return null;
    }

    // parse html string into element
    public static Node asNode(String html) {
        return unwrapAsNode(wrapHTML(html));
    }

    /**
     * Replace node in given html with id and element from nodeIdMap
     */
    public static String replacePlaceholderWithNode(
            Map<String, Node> nodeIdMap, String html) {
        Element element = wrapHTML(html);
        for (Map.Entry<String, Node> entry : nodeIdMap.entrySet()) {
            String id = entry.getKey();
            Node replacementNode = entry.getValue();
            element.select("#" + id).first().replaceWith(replacementNode);
        }
        return unwrapAsNode(element).outerHtml();
    }

    private static String getWrapperId() {
        return ID_PREFIX + "-" + WRAPPER_ID;
    }

    private static void replaceNodeWithPlaceholder(
            Map<String, Node> placeholderIdMap, int index,
            int counter, Element element) {
        String id = generatePlaceholderId(index, counter);
        placeholderIdMap.put(id, element.clone());
        Element placeholderElement = generatePlaceholderNode(id);
        element.replaceWith(placeholderElement);
    }
}
