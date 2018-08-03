package org.zanata.magpie.service;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;
import org.junit.Test;
import com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslatableHTMLNodeTest {

    @Test
    public void testConstructor() {
        Map<String, Node> map = new HashMap<>();
        Element doc = new Element(Tag.valueOf("span"), "", new Attributes());
        TranslatableNodeList node = new TranslatableNodeList(
                ImmutableList.of(doc), map);
        assertThat(node.getPlaceholderIdMap()).isEqualTo(map);
        assertThat(node.getHtml()).isEqualTo(doc.outerHtml());
    }
}
