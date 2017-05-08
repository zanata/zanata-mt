package org.zanata.mt.util;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Test;
import org.zanata.mt.model.TranslatableHTMLNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        assertWrapAndUnwrapHTML("<html><body>test</body></html>");
        assertWrapAndUnwrapHTML("<body>test</body>");
        assertWrapAndUnwrapHTML("<div>test</div>");
        assertWrapAndUnwrapHTML("test");
    }

    @Test
    public void asElement() {
        String html = "<html><body>test</body></html>";
        Node node = ArticleUtil.asElement(html);
        assertThat(node).isNotNull().extracting(Node::outerHtml)
                .contains(html);
    }

    private void assertWrapAndUnwrapHTML(String html) {
        Element wrappedElement = ArticleUtil.wrapHTML(html);
        assertThat(wrappedElement.outerHtml()).isNotEqualTo(html);
        assertThat(wrappedElement.outerHtml().length())
                .isGreaterThan(html.length());

        List<Node> unwrappedNodes = ArticleUtil.unwrapAsNodes(wrappedElement);
        assertThat(unwrappedNodes).isNotEmpty();

        String unwrappedHTML =
                unwrappedNodes.stream().map(node -> node.outerHtml())
                        .collect(Collectors.joining());

        assertThat(unwrappedHTML.replaceAll("\n", "")
                .replaceAll(" ", "")).isEqualTo(html);
    }

    @Test
    public void replaceNonTranslatableNode() {
        String node1 = "<span translate=\"no\">do not translate</span>";
        String node4 = "<span class=\"notranslate\">do not translate</span>";
        String node5 = "<span id=\"private-notes-testing\">do not translate</span>";
        String node6 = "<span>translate this</span>";
        String node7 = "<p>translate this</p>";
        String html = "<div>" + node1 + node4 + node5 + node6 +
                node7 + "</div>";
        TranslatableHTMLNode
                node = ArticleUtil.replaceNonTranslatableNode(1, html);
        assertThat(node.getPlaceholderIdMap()).hasSize(3);
        assertThat(node.getPlaceholderIdMap().values()).extracting(
                Node::toString)
                .contains(node1, node4, node5)
                .doesNotContain(node6, node7);
        assertThat(node.getHtml()).doesNotContain(node1)
                .doesNotContain(node4)
                .doesNotContain(node5).contains(node6, node7);
    }

    @Test
    public void test() {
        String html = "<code class=\"notranslate\">The ski is blue.</code><p>This is a test of what kinds of fruit that you like best.  Oranges? Grapes?</p>";
        TranslatableHTMLNode node = ArticleUtil.replaceNonTranslatableNode(1, html);
        System.out.println(node.getHtml());
    }


    @Test
    public void replacePlaceholderWithNode() {
        Map<String, Node> nodeIdMap = new HashMap<>();
        String html = "<div>";
        for (int i = 0; i < 5; i++) {
            Attributes attrs = new Attributes();
            String id = "id" + i;
            attrs.put("id", id);
            Element ele = new Element(Tag.valueOf("span"), "", attrs);
            ele.append("The original node");
            nodeIdMap.put(id, ele);

            Element placeholder = ArticleUtil.generatePlaceholderNode(id);
            html += placeholder.outerHtml();
        }
        html += "</div>";

        String results = ArticleUtil.replacePlaceholderWithNode(nodeIdMap, html);
        for (Node originalNode: nodeIdMap.values()) {
            assertThat(results).contains(originalNode.outerHtml());
        }
    }
}
