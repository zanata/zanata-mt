package org.zanata.magpie.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.zanata.magpie.service.TranslatableNodeList;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import kotlin.Pair;

/**
 * Utility class for Article
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class ArticleUtil {
    private final static String ID_PREFIX = "ZNTA";
    private final static String WRAPPER_ID = "WRAP";

    // HTML tag that will not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_HTML_NODE = ImmutableList
            .of("script", "#text", "code", "col", "colgroup", "embed",
                    "#comment", "image", "map", "media", "meta", "source",
                    "xml");

    // Node with attribute that will not be translated (XML or HTML)
    private final static ImmutableList<Pair<String, String>> NON_TRANSLATABLE_ATTRIBUTE =
            ImmutableList.of(new Pair<>("translate", "no"));

    // Node with css class that wil not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_HTML_CLASS =
            ImmutableList.of("notranslate");

    // Node with id prefix that will not be translated
    private final static ImmutableList<String> NON_TRANSLATABLE_HTML_ID =
            ImmutableList.of("private-notes");

    @SuppressWarnings("unused")
    private ArticleUtil() {
    }

    /**
     * Replace non-translatable node with placeholder
     *
     * @param prefix - index of the node, used to generate unique id for placeholder
     * @param html - html string
     */
    public static TranslatableNodeList replaceNonTranslatableNodeHTML(int prefix,
            String html) {
        Element document = wrapHTML(html);
        Map<String, Node> placeholderIdMap =
                createPlaceholdersHTML(prefix, document);
        return new TranslatableNodeList(unwrapAsNodes(document),
                placeholderIdMap);
    }

    @org.jetbrains.annotations.NotNull
    private static Map<String, Node> createPlaceholdersHTML(int prefix,
            Element document) {
        Map<String, Node> placeholderIdMap = new HashMap<>();

        int counter = 0;
        counter = usePlaceholdersForAttributes(document,
                placeholderIdMap, prefix, counter);
        counter = usePlaceholdersForHTMLNodes(document,
                placeholderIdMap, prefix, counter);
        counter = usePlaceholdersForHTMLClass(document,
                placeholderIdMap, prefix, counter);
        usePlaceholdersForHtmlIds(document,
                placeholderIdMap, prefix, counter);
        return placeholderIdMap;
    }

    /**
     * @param criteria a list of tags/attributes/ids/classes whose Elements want to find
     * @param elementFinder takes one of the criteria and returns matching Elements
     * @param <T> the type of element criteria (String or Pair&lt;String, String&gt;)
     * @return the new placeholder counter
     */
    private static <T> int usePlaceholders(
            Map<String, Node> placeholderIdMap, int prefix,
            int counter, List<T> criteria,
            Function<T, Elements> elementFinder) {
        for (T criterion : criteria) {
            for (Element element : elementFinder.apply(criterion)) {
                replaceNodeWithPlaceholder(placeholderIdMap, prefix,
                        counter, element);
                counter++;
            }
        }
        return counter;
    }

    private static int usePlaceholdersForAttributes(
            Element document, Map<String, Node> placeholderIdMap, int prefix,
            int counter) {
        return usePlaceholders(placeholderIdMap, prefix, counter,
                NON_TRANSLATABLE_ATTRIBUTE, attr ->
                        getElementsByAttributeValue(document, attr)
        );
    }

    private static Elements getElementsByAttributeValue(
            Element document, Pair<String, String> attr) {
        return document.getElementsByAttributeValueContaining(
                attr.getFirst(), attr.getSecond());
    }

    private static int usePlaceholdersForHTMLNodes(
            Element document, Map<String, Node> placeholderIdMap, int prefix,
            int counter) {
        return usePlaceholders(placeholderIdMap, prefix,
                counter, NON_TRANSLATABLE_HTML_NODE,
                document::getElementsByTag);
    }

    private static int usePlaceholdersForHTMLClass(
            Element document, Map<String, Node> placeholderIdMap, int prefix,
            int counter) {
        return usePlaceholders(placeholderIdMap, prefix, counter,
                NON_TRANSLATABLE_HTML_CLASS, document::getElementsByClass);
    }

    private static int usePlaceholdersForHtmlIds(
            Element document, Map<String, Node> placeholderIdMap, int prefix,
            int counter) {
        return usePlaceholders(placeholderIdMap, prefix, counter,
                NON_TRANSLATABLE_HTML_ID,
                id -> document.select("[id^='" + id + "']"));
    }

    /**
     * Replace non-translatable node with placeholder
     *
     * @param prefix - index of the node, used to generate unique id for placeholder
     * @param xml - xml string
     */
    public static TranslatableNodeList replaceNonTranslatableNodeXML(int prefix,
            String xml) {
        Element document = wrapXML(xml);
        Map<String, Node> placeholderIdMap =
                createPlaceholdersXML(prefix, document);
        return new TranslatableNodeList(unwrapAsNodes(document),
                placeholderIdMap);
    }

    @org.jetbrains.annotations.NotNull
    private static Map<String, Node> createPlaceholdersXML(int prefix,
            Element document) {
        Map<String, Node> placeholderIdMap = new HashMap<>();

        int counter = 0;
        // don't apply tag/css/id to XML contents
        usePlaceholdersForAttributes(document,
                placeholderIdMap, prefix,
                counter);
        return placeholderIdMap;
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
    public static Document wrapHTML(String html) {
        String wrapHTML = html;
        if (!html.startsWith("<html>") && !html.startsWith("<body>")) {
            wrapHTML = "<div id='" + getWrapperId() + "'>" + html + "</div>";
        }
        Document doc = Parser.parseBodyFragment(wrapHTML, "");
        // Configure this document (and its nodes) to avoid pretty printing in node.outerHtml()
        doc.outputSettings().indentAmount(0).prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.html);
        return doc;
    }

    /**
     * Wrap given html around MT wrapper for easy extraction
     *
     * IMPORTANT: This assumes the html is wrap in single html node
     */
    public static Document wrapXML(String html) {
        Document doc = Jsoup.parse(html, "", Parser.xmlParser());

        // Configure this document (and its nodes) to avoid pretty printing in node.outerHtml()
        doc.outputSettings().indentAmount(0).prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.xml);
//                .escapeMode(Entities.EscapeMode.xhtml);
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
    public static List<Node> unwrapAsElements(@NotNull Element element) {
        Element wrapper = element.select("#" + getWrapperId()).first();
        if (wrapper != null) {
            return wrapper.childNodes();
        }
        return Lists.newArrayList(element);
    }

    // parse html string into element
    static @Nullable Node asElementHTML(String html) {
        return asElement(html, ArticleUtil::wrapHTML);
    }

    // parse html string into element
    public static @Nullable Node asElement(String html, Function<String, Element> toElement) {
        if (StringUtils.isBlank(html)) {
            return null;
        }
        List<Node> elements = unwrapAsElements(toElement.apply(html));
        return elements.isEmpty() ? null : elements.get(0);
    }

    /**
     * Replace node in given html with id and element from nodeIdMap
     */
    static String replacePlaceholderWithNodeHTML(
            Map<String, Node> nodeIdMap, String html) {
        return replacePlaceholderWithNode(nodeIdMap, html, ArticleUtil::wrapHTML);
    }

    /**
     * Replace node in given xml with id and element from nodeIdMap
     */
    public static String replacePlaceholderWithNode(
            Map<String, Node> nodeIdMap, String xml, Function<String, Element> toElement) {
        Element element = toElement.apply(xml);
        for (Map.Entry<String, Node> entry : nodeIdMap.entrySet()) {
            String id = entry.getKey();
            Node replacementNode = entry.getValue();
//            elements.stream()
//                    .filter(el -> el.attr("id").equals(id))
//                    .findFirst()
//                    .ifPresent(it -> it.replaceWith(replacementNode));
            element.select("#" + id).first().replaceWith(replacementNode);
        }
        return unwrapAsNodes(element).stream().map(Node::outerHtml)
                .collect(Collectors.joining());
    }

    private static String getWrapperId() {
        return ID_PREFIX + "-" + WRAPPER_ID;
    }

    private static void replaceNodeWithPlaceholder(
            Map<String, Node> placeholderIdMap, int prefix,
            int counter, Element element) {
        String id = generatePlaceholderId(prefix, counter);
        placeholderIdMap.put(id, element.clone());
        Element placeholderElement = generatePlaceholderNode(id);
        element.replaceWith(placeholderElement);
    }
}
