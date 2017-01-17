package org.zanata.mt.article.kcs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.mt.article.kcs.KCSUtil.ID_PREFIX;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class KCSUtilTest {
    @Test
    public void testGetRawCodePreElements() {
        String html =
            "<html><div class=\"code-raw testing1 classes\"><span></span><pre></pre></div><div><div class=\"code-raw testing classes\"></div></div></html>";
        Document document = Jsoup.parse(html);
        Elements elements = KCSUtil.getRawCodePreElements(document);
        assertThat(elements.size()).isEqualTo(1);
    }

    @Test
    public void testGetNonTranslatableNode() {
        String name = ID_PREFIX + "-" + String.valueOf(1 + "_" + 1);
        String expectedName = KCSUtil.generateCodeElementName(1, 1);
        Element nonTranslatableNode = KCSUtil.generateNonTranslatableNode(name);
        assertThat(nonTranslatableNode.attr("name")).isEqualTo(expectedName);
        assertThat(nonTranslatableNode.tagName()).isEqualTo("meta");
        assertThat(nonTranslatableNode.attr("translate")).isEqualTo("no");
    }

    @Test
    public void testIsPrivateNote() {
        String id = "private-notes-testing";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(KCSUtil.isPrivateNotes(element)).isTrue();
    }

    @Test
    public void testIsNotPrivateNote() {
        String id = "testing-private-notes";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(KCSUtil.isPrivateNotes(element)).isFalse();
    }
}
