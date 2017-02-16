package org.zanata.mt.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DomUtilTest {

    @Test
    public void testParseAsElement() {
        Elements elements = DomUtil.parseAsElement(
                "<html><body><tag>content to return</tag></body></html>");
        assertThat(elements).isNotNull().isNotEmpty();
    }

    @Test
    public void testParseAsElementEmptyBody() {
        Elements elements = DomUtil.parseAsElement(
                "<html></html>");
        assertThat(elements).isNull();
    }

    @Test
    public void testParseAsElementEmptyBodyChildren() {
        Elements elements = DomUtil.parseAsElement(
                "<html><body></body></html>");
        assertThat(elements).isNull();
    }

    @Test
    public void testExtractEmptyBodyContentHTML() {
        String html = "<html><body></body></html>";
        Document doc = Jsoup.parse(html);
        String bodyHtml = DomUtil.extractBodyContentHTML(doc);
        assertThat(bodyHtml).isEqualTo("");
    }

    @Test
    public void testExtractBodyHTML() {
        String html = "<html><body>test body</body></html>";
        Document doc = Jsoup.parse(html);
        String bodyHtml = DomUtil.extractBodyContentHTML(doc);
        assertThat(bodyHtml).isEqualTo("test body");
    }
}
