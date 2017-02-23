package org.zanata.mt.api.dto;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleTest {

    @Test
    public void testEmptyConstructor() {
        Document article = new Document();
    }

    @Test
    public void testConstructor() {
        List<TypeString> lists = Lists.newArrayList(
                new TypeString("test", "text/plain", "meta"));
        Document article = new Document(lists, "http://localhost", "en");

        assertThat(article.getBackendId()).isNull();
        assertThat(article.getContents()).isEqualTo(lists);
        assertThat(article.getLocale()).isEqualTo("en");
        assertThat(article.getUrl()).isEqualTo("http://localhost");
    }

    @Test
    public void testConstructor2() {
        List<TypeString> lists = Lists.newArrayList(
                new TypeString("test", "text/plain", "meta"));
        Document article = new Document(lists, "http://localhost", "en", "backendId");

        assertThat(article.getContents()).isEqualTo(lists);
        assertThat(article.getLocale()).isEqualTo("en");
        assertThat(article.getUrl()).isEqualTo("http://localhost");
        assertThat(article.getBackendId()).isEqualTo("backendId");
    }

    @Test
    public void testUrl() {
        Document article = new Document();
        article.setUrl("http://localhost");
        assertThat(article.getUrl()).isEqualTo("http://localhost");
   }

    @Test
    public void testContents() {
        Document article = new Document();
        List<TypeString> lists =
                Lists.newArrayList(
                        new TypeString("test", "text/plain", "meta"));
        article.setContents(lists);
        assertThat(article.getContents()).isEqualTo(lists);
    }

    @Test
    public void testLocale() {
        Document article = new Document();
        article.setLocale("en");
        assertThat(article.getLocale()).isEqualTo("en");
    }

    @Test
    public void testBackendId() {
        Document article = new Document();
        article.setBackendId("backendId");
        assertThat(article.getBackendId()).isEqualTo("backendId");
    }

    @Test
    public void testEqualsAndHashcode() {
        Document article1 = getDefaultArticle();

        // change backend id
        Document article2 = getDefaultArticle();
        article2.setBackendId("backendId2");

        assertThat(article1.equals(article2)).isFalse();
        assertThat(article1.hashCode()).isNotEqualTo(article2.hashCode());

        // change locale
        article2 = getDefaultArticle();
        article2.setLocale("fr");

        assertThat(article1.equals(article2)).isFalse();
        assertThat(article1.hashCode()).isNotEqualTo(article2.hashCode());

        // change url
        article2 = getDefaultArticle();
        article2.setUrl("http://localhost2");

        assertThat(article1.equals(article2)).isFalse();
        assertThat(article1.hashCode()).isNotEqualTo(article2.hashCode());

        // change contents
        article2 = getDefaultArticle();
        article2.setContents(
                Lists.newArrayList(
                        new TypeString("test2", "text/plain", "meta")));

        assertThat(article1.equals(article2)).isFalse();
        assertThat(article1.hashCode()).isNotEqualTo(article2.hashCode());

        article2 = getDefaultArticle();

        assertThat(article1.equals(article2)).isTrue();
        assertThat(article1.hashCode()).isEqualTo(article2.hashCode());
    }

    private Document getDefaultArticle() {
        List<TypeString> lists =
                Lists.newArrayList(
                        new TypeString("test", "text/plain", "meta"));
        return new Document(lists, "http://localhost", "en", "backendId");
    }
}
