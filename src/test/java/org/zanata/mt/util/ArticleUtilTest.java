package org.zanata.mt.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleUtilTest {
    @Test
    public void containsPrivateNotes() {
        String html = "<html><div></div></html>";
        String html2 = "<html><div id='private-notes-testing'></div></html>";
        assertThat(ArticleUtil.containsPrivateNotes(html)).isFalse();
        assertThat(ArticleUtil.containsPrivateNotes(html2)).isTrue();
    }

    @Test
    public void containsKCSCodeSection() {
        String html = "<html><div></div></html>";
        String html2 = "<html><div id='code-raw'></div></html>";
        assertThat(ArticleUtil.containsKCSCodeSection(html)).isFalse();
        assertThat(ArticleUtil.containsKCSCodeSection(html2)).isTrue();
    }

    @Test
    public void containsNonTranslatableNodeRecognised() {
        String html = "<div id='code-raw' translate='no'></div>";
        assertThat(ArticleUtil.containsNonTranslatableNode(html)).isTrue();
    }

    @Test
    public void translatableNodeRecognised() {
        String html = "<div id='code-raw' translate='yes'></div>";
        assertThat(ArticleUtil.containsNonTranslatableNode(html)).isFalse();
    }

    @Test
    public void translatableNodeRecognised2() {
        String html = "<div id='code-raw'></div>";
        assertThat(ArticleUtil.containsNonTranslatableNode(html)).isFalse();
    }
}
