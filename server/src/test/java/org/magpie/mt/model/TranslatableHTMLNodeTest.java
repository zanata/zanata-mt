package org.magpie.mt.model;

import org.assertj.core.util.Lists;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslatableHTMLNodeTest {

    @Test
    public void testConstructor() {
        Map<String, Node> map = new HashMap<>();
        Element doc = new Element(Tag.valueOf("span"), "", new Attributes());
        TranslatableHTMLNode node = new TranslatableHTMLNode(
                Lists.newArrayList(doc), map);
        assertThat(node.getPlaceholderIdMap()).isEqualTo(map);
        assertThat(node.getHtml()).isEqualTo(doc.outerHtml());
    }
}
