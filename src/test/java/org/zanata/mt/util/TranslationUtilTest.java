package org.zanata.mt.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationUtilTest {
    @Test
    public void testGenerateHash() {
        String test = "testing";
        LocaleId localeId = LocaleId.DE;
        String expectedHash = "78abe7884fa62f37e99113e843c1949a";

        String hash = TranslationUtil.generateHash(test, localeId);
        assertThat(hash).isEqualTo(expectedHash);
    }

    @Test
    public void testGetRawCodePreElements() {
        String html =
                "<html><div class=\"code-raw testing1 classes\"><span></span><pre></pre></div><div><div class=\"code-raw testing classes\"></div></div></html>";
        Document document = Jsoup.parse(html);
        Elements elements = TranslationUtil.getRawCodePreElements(document);
        assertThat(elements.size()).isEqualTo(1);
    }

    @Test
    public void testGetNonTranslatableNode() {
        String id = "testing-id";
        Element nonTranslatableNode = TranslationUtil.getNonTranslatableNode(id);
        assertThat(nonTranslatableNode.id()).isEqualTo(id);
        assertThat(nonTranslatableNode.tagName()).isEqualTo("meta");
        assertThat(nonTranslatableNode.attr("translate")).isEqualTo("no");
    }

    @Test
    public void testIsPrivateNote() {
        String id = "private-notes-testing";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(TranslationUtil.isPrivateNotes(element)).isTrue();
    }

    @Test
    public void testIsNotPrivateNote() {
        String id = "testing-private-notes";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(TranslationUtil.isPrivateNotes(element)).isFalse();
    }
}
