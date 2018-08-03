package org.zanata.magpie.util;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Test;
import org.zanata.magpie.service.TranslatableNodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleUtilTest {

    @Test
    public void generatePlaceholderId() {
        int prefix = 1;
        int index = 2;
        assertThat(ArticleUtil.generatePlaceholderId(prefix, index))
                .contains(String.valueOf(prefix), String.valueOf(index));
    }

    @Test
    public void generatePlaceholderNode() {
        String id = "test1";
        Element ele = ArticleUtil.generatePlaceholderNode(id);
        assertThat(ele).isNotNull();
        assertThat(ele.id()).isEqualTo(id);
        assertThat(ele.attr("translate")).isEqualTo("no");
    }

    @Test
    public void wrapUnWrapHTML() {
        assertWrapAndUnwrapHTML("<html><head></head><body>test</body></html>");
        assertWrapAndUnwrapHTML("<div>test</div>");
        assertWrapAndUnwrapHTML("<div><br>test</div>");
        assertWrapAndUnwrapHTML("test");
    }

    @Test
    public void wrapUnWrapXML() {
        assertWrapAndUnwrapXML("<div><br />test</div>");
        assertWrapAndUnwrapXML("The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>]");

    }

    @Test
    public void asElementHTML() {
        String html = "<html><head></head><body>test</body></html>";
        Node node = ArticleUtil.asElementHTML(html);
        assertThat(node).isNotNull().extracting(Node::outerHtml)
                .contains(html);
    }

    @Test
    public void asElementEmptyContentHTML() {
        String html = "";
        Node node = ArticleUtil.asElementHTML(html);
        assertThat(node).isNull();
    }

    private void assertWrapAndUnwrapHTML(String html) {
        assertWrapAndUnwrap(html, ArticleUtil::wrapHTML);
    }

    private void assertWrapAndUnwrapXML(String html) {
        assertWrapAndUnwrap(html, ArticleUtil::wrapXML);
    }

    private void assertWrapAndUnwrap(String html, Function<String, Element> toElement) {
        Element wrappedElement = toElement.apply(html);

        List<Node> unwrappedNodes = ArticleUtil.unwrapAsNodes(wrappedElement);
        assertThat(unwrappedNodes).isNotEmpty();

        String unwrappedHTML =
                unwrappedNodes.stream().map(Node::outerHtml)
                        .collect(Collectors.joining());
        assertThat(unwrappedHTML).isEqualTo(html);
    }

    @Test
    public void replaceNonTranslatableNodeHTML() {
        String node1NoTranslate = "<span translate=\"no\">do not translate</span>";
        String node2 = "<em>translate</em>";
        String node4NoTranslate = "<span class=\"notranslate\">do not translate</span>";
        String node5NoTranslate = "<span id=\"private-notes-testing\">do not translate</span>";
        String node6 = "<span>translate this</span>";
        String node7 = "<p>translate this</p>";
        String htmlToProcess = node1NoTranslate + node2 + node4NoTranslate + node5NoTranslate + node6 +
                node7;
        TranslatableNodeList
                node = ArticleUtil.replaceNonTranslatableNodeHTML(1, htmlToProcess);
        assertThat(node.getPlaceholderIdMap().values())
                .extracting(Node::toString)
                .containsExactly(node1NoTranslate, node4NoTranslate, node5NoTranslate);
        assertThat(node.getHtml())
                .contains(node2, node6, node7)
                .doesNotContain(node1NoTranslate)
                .doesNotContain(node4NoTranslate)
                .doesNotContain(node5NoTranslate);
    }

    @Test
    public void replaceNonTranslatableNodeWhenEverythingIsTranslatableXML() {
        String htmlToProcess = "The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>]";
        TranslatableNodeList
                node = ArticleUtil.replaceNonTranslatableNodeXML(1, htmlToProcess);
        assertThat(node.getPlaceholderIdMap()).isEmpty();
        String actualHtml = node.getHtml();
        assertThat(actualHtml).isEqualTo(htmlToProcess);
    }

    @Test
    public void replacePlaceholderWithNodeHTML() {
        Map<String, Node> nodeIdMap = new HashMap<>();
        StringBuilder html = new StringBuilder("<div>");
        for (int i = 0; i < 5; i++) {
            Attributes attrs = new Attributes();
            String id = "id_" + i;
            attrs.put("id", id);
            Element ele = new Element(Tag.valueOf("span"), "", attrs);
            ele.append("The original node");
            nodeIdMap.put(id, ele);

            Element placeholder = ArticleUtil.generatePlaceholderNode(id);
            html.append(placeholder.outerHtml());
        }
        html.append("</div>");

        String results = ArticleUtil.replacePlaceholderWithNodeHTML(nodeIdMap,
                html.toString());
        for (Node originalNode: nodeIdMap.values()) {
            assertThat(results).contains(originalNode.outerHtml());
        }
    }
}
