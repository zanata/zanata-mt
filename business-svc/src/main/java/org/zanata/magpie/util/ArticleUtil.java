package org.zanata.magpie.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.zanata.magpie.service.TranslatableHTMLNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Utility class for Article
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ArticleUtil {
    private final static String ID_PREFIX = "ZNTA";
    private final static String WRAPPER_ID = "WRAP";

    // HTML tag that will not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_NODE = ImmutableList
            .of("script", "#text", "code", "col", "colgroup", "embed",
                    "#comment", "image", "map", "media", "meta", "source",
                    "xml");

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
        return new TranslatableHTMLNode(unwrapAsNodes(document),
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
     * Wrap given html around MT wrapper for easy extraction
     *
     * IMPORTANT: This assumes the html is wrap in single html node
     */
    public static Element wrapHTML(String html) {
        String wrapHTML = html;
        if (!html.startsWith("<html>") && !html.startsWith("<body>")) {
            wrapHTML = "<div id='" + getWrapperId() + "'>" + html + "</div>";
        }
        Document doc = Jsoup.parseBodyFragment(wrapHTML);
        doc.outputSettings().indentAmount(0).prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.html);
        return doc;
    }

    /**
     * Unwrap a wrapped element inside MT wrapper.
     */
    public static List<Node> unwrapAsNodes(@NotNull Element element) {
        Element wrapper = element.select("#" + getWrapperId()).first();
        if (wrapper != null) {
            return wrapper.childNodes();
        }
        return Lists.newArrayList(element);
    }

    /**
     * Unwrap a wrapped element inside MT wrapper.
     */
    public static List<Element> unwrapAsElements(@NotNull Element element) {
        Element wrapper = element.select("#" + getWrapperId()).first();
        if (wrapper != null) {
            return wrapper.children();
        }
        return Lists.newArrayList(element);
    }

    // parse html string into element
    public static @Nullable Element asElement(String html) {
        if (StringUtils.isBlank(html)) {
            return null;
        }
        List<Element> elements = unwrapAsElements(wrapHTML(html));
        return elements.isEmpty() ? null :  elements.get(0);
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
        return unwrapAsNodes(element).stream().map(node -> node.outerHtml())
                .collect(Collectors.joining());
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
