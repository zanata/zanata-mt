package org.zanata.mt.util;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Test;
import org.zanata.mt.model.TranslatableHTMLNode;

import java.util.HashMap;
import java.util.Map;

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

        Node unwrappedNode = ArticleUtil.unwrapAsNode(wrappedElement);
        assertThat(unwrappedNode).isNotEqualTo(wrappedElement);
        String unwrappedHTML = unwrappedNode.outerHtml();

        assertThat(unwrappedHTML.replaceAll("\n", "")
                .replaceAll(" ", "")).isEqualTo(html);
    }

    @Test
    public void replaceNonTranslatableNode() {
        String node1 = "<span translate=\"no\">do not translate</span>";
        String node2 = "<pre>do not translate</pre>";
        String node3 = "<em>do not translate</em>";
        String node4 = "<span class=\"notranslate\">do not translate</span>";
        String node5 = "<span id=\"private-notes-testing\">do not translate</span>";
        String node6 = "<span>translate this</span>";
        String node7 = "<p>translate this</p>";
        String html = "<div>" + node1 + node2 + node3 + node4 + node5 + node6 +
                node7 + "</div>";
        TranslatableHTMLNode
                node = ArticleUtil.replaceNonTranslatableNode(1, html);
        assertThat(node.getPlaceholderIdMap()).hasSize(5);
        assertThat(node.getPlaceholderIdMap().values()).extracting(
                Node::toString)
                .contains(node1, node2, node3, node4, node5)
                .doesNotContain(node6, node7);
        assertThat(node.getHtml()).doesNotContain(node1).doesNotContain(node2)
                .doesNotContain(node3).doesNotContain(node4)
                .doesNotContain(node5).contains(node6, node7);
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
