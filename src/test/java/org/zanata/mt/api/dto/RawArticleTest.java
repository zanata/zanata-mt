package org.zanata.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class RawArticleTest {

    @Test
    public void testEmptyConstructor() {
        RawArticle rawArticle = new RawArticle();
    }

    @Test
    public void testConstructor() {
        RawArticle rawArticle =
                new RawArticle("title", "contentHtml", "http://localhost",
                        "articleType", "en");

        assertThat(rawArticle.getTitleText()).isEqualTo("title");
        assertThat(rawArticle.getContentHTML()).isEqualTo("contentHtml");
        assertThat(rawArticle.getUrl()).isEqualTo("http://localhost");
        assertThat(rawArticle.getArticleType()).isEqualTo("articleType");
        assertThat(rawArticle.getLocale()).isEqualTo("en");
        assertThat(rawArticle.getBackendId()).isNull();
    }

    @Test
    public void testConstructor2() {
        RawArticle rawArticle =
                new RawArticle("title", "contentHtml", "http://localhost",
                        "articleType", "en", "backendId");

        assertThat(rawArticle.getTitleText()).isEqualTo("title");
        assertThat(rawArticle.getContentHTML()).isEqualTo("contentHtml");
        assertThat(rawArticle.getUrl()).isEqualTo("http://localhost");
        assertThat(rawArticle.getArticleType()).isEqualTo("articleType");
        assertThat(rawArticle.getLocale()).isEqualTo("en");
        assertThat(rawArticle.getBackendId()).isEqualTo("backendId");
    }

    @Test
    public void testTitleText() {
        RawArticle rawArticle = new RawArticle();
        rawArticle.setTitleText("title text");
        assertThat(rawArticle.getTitleText()).isEqualTo("title text");
    }

    @Test
    public void testContentHtml() {
        RawArticle rawArticle = new RawArticle();
        rawArticle.setContentHTML("content html");
        assertThat(rawArticle.getContentHTML()).isEqualTo("content html");
    }

    @Test
    public void testUrl() {
        RawArticle rawArticle = new RawArticle();
        rawArticle.setUrl("http://localhost");
        assertThat(rawArticle.getUrl()).isEqualTo("http://localhost");
    }

    @Test
    public void testArticleType() {
        RawArticle rawArticle = new RawArticle();
        rawArticle.setArticleType("articleType");
        assertThat(rawArticle.getArticleType()).isEqualTo("articleType");
    }

    @Test
    public void testLocale() {
        RawArticle rawArticle = new RawArticle();
        rawArticle.setLocale("fr");
        assertThat(rawArticle.getLocale()).isEqualTo("fr");
    }

    @Test
    public void testBackendId() {
        RawArticle rawArticle = new RawArticle();
        rawArticle.setBackendId("backendId");
        assertThat(rawArticle.getBackendId()).isEqualTo("backendId");
    }

    @Test
    public void testToString() {
        RawArticle rawArticle1 =
                new RawArticle("title", "contentHtml", "http://localhost",
                        "articleType", "en", "backendId");

        String rawArticle2 =
                new RawArticle("title", "contentHtml", "http://localhost",
                        "articleType", "en", "backendId").toString();
        assertThat(rawArticle1.toString()).isNotNull().isEqualTo(rawArticle2);
    }

    @Test
    public void testEqualsAndHashcode() {
        RawArticle rawArticle1 = getDefault();

        // change title
        RawArticle rawArticle2 = getDefault();
        rawArticle2.setTitleText("title1");

        assertThat(rawArticle1.equals(rawArticle2)).isFalse();
        assertThat(rawArticle1.hashCode()).isNotEqualTo(rawArticle2.hashCode());


        // change content html
        rawArticle2 = getDefault();
        rawArticle2.setContentHTML("contentHtml2");

        assertThat(rawArticle1.equals(rawArticle2)).isFalse();
        assertThat(rawArticle1.hashCode()).isNotEqualTo(rawArticle2.hashCode());

        // change url
        rawArticle2 = getDefault();
        rawArticle2.setUrl("http://localhost2");

        assertThat(rawArticle1.equals(rawArticle2)).isFalse();
        assertThat(rawArticle1.hashCode()).isNotEqualTo(rawArticle2.hashCode());

        // change article type
        rawArticle2 = getDefault();
        rawArticle2.setArticleType("articleType2");

        assertThat(rawArticle1.equals(rawArticle2)).isFalse();
        assertThat(rawArticle1.hashCode()).isNotEqualTo(rawArticle2.hashCode());

        // change locale
        rawArticle2 = getDefault();
        rawArticle2.setLocale("fr");

        assertThat(rawArticle1.equals(rawArticle2)).isFalse();
        assertThat(rawArticle1.hashCode()).isNotEqualTo(rawArticle2.hashCode());

        // change backendId
        rawArticle2 = getDefault();
        rawArticle2.setBackendId("backendId2");

        assertThat(rawArticle1.equals(rawArticle2)).isFalse();
        assertThat(rawArticle1.hashCode()).isNotEqualTo(rawArticle2.hashCode());

        rawArticle2 = getDefault();

        assertThat(rawArticle1.equals(rawArticle2)).isTrue();
        assertThat(rawArticle1.hashCode()).isEqualTo(rawArticle2.hashCode());
    }

    private RawArticle getDefault() {
        return new RawArticle("title", "contentHtml", "http://localhost",
                        "articleType", "en");
    }
}
