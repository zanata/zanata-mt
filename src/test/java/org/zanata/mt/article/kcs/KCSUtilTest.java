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
import static org.zanata.mt.article.kcs.KCSUtil.generateCodeElementName;
import static org.zanata.mt.article.kcs.KCSUtil.generateNonTranslatableNode;
import static org.zanata.mt.article.kcs.KCSUtil.getRawCodePreElements;
import static org.zanata.mt.article.kcs.KCSUtil.isPrivateNotes;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class KCSUtilTest {
    @Test
    public void rawCodePreElementsAreFound() {
        String pre = "<pre>coding which should not be translated</pre>";
        String html =
            "<html><div class=\"code-raw testing1 classes\"><span></span>" + pre + "</div><div><div class=\"code-raw testing classes\"></div></div></html>";
        Document document = Jsoup.parse(html);
        Elements elements = getRawCodePreElements(document);
        assertThat(elements).hasSize(1).extracting(Element::outerHtml)
                .contains(pre);
    }

    @Test
    public void testGenerateNonTranslatable() {
        String name = ID_PREFIX + "-" + String.valueOf(1);
        String expectedName = generateCodeElementName(1);
        Element nonTranslatableNode = generateNonTranslatableNode(name);
        assertThat(nonTranslatableNode.attr("name")).isEqualTo(expectedName);
        assertThat(nonTranslatableNode.tagName()).isEqualTo("meta");
        assertThat(nonTranslatableNode.attr("translate")).isEqualTo("no");
    }

    @Test
    public void privateNotesAreRecognised() {
        String id = "private-notes-testing";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(isPrivateNotes(element)).isTrue();
    }

    @Test
    public void nonPrivateNotesAreRecognised() {
        String id = "testing-private-notes";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(isPrivateNotes(element)).isFalse();
    }
}
