package org.zanata.mt.util;

import org.junit.Test;
import org.zanata.mt.api.dto.TypeString;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleUtilTest {
    @Test
    public void replacePrivateNotes() {
        String html = "<div><span id=\"private-notes-testing\">To be replace with</span></div>";
        String replacingHtml = "<div>   replace with this  </div>";

        TypeString typeString = new TypeString(html, "text/plain", null);
        ArticleUtil.replacePrivateNotes(typeString, replacingHtml);

        assertThat(typeString.getValue().trim().replaceAll("\n", "")
                .replaceAll(">\\s+<", "><")).contains(replacingHtml);
    }

    @Test
    public void replaceKCSCodeSection() {
        String html = "<div><span id=\"code-raw\">To be replace with</span></div>";
        String replacingHtml = "<div>   replace with this  </div>";

        TypeString typeString = new TypeString(html, "text/plain", null);
        ArticleUtil.replaceKCSCodeSection(typeString, replacingHtml);

        assertThat(typeString.getValue().trim().replaceAll("\n", "")
                .replaceAll(">\\s+<", "><")).contains(replacingHtml);
    }

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
    public void generateNonTranslatableHtml() {
        String id = "1";
        String nodeId = ArticleUtil.generateNodeId(id);
        String html = ArticleUtil.generateNonTranslatableHtml(id);
        assertThat(html).contains("translate='no'").contains("id='" + nodeId + "'");
    }

    @Test
    public void replaceByNodeId() {
        String id = "1";
        String nodeId = ArticleUtil.generateNodeId(id);

        String originalHtml = "<div><pre>this pre is not to be translated</pre></div>";
        String translatedHtml = "<div><span id='" + nodeId + "'>This is translated html</span></div>";

        String expectedHtml = "<div><div><pre>this pre is not to be translated</pre></div></div>";
        TypeString typeString = new TypeString(translatedHtml, "text/plain", null);

        ArticleUtil.replaceNodeById(id, originalHtml, typeString);
        assertThat(typeString.getValue().trim().replaceAll("\n", "")
                .replaceAll(">\\s+<", "><")).isEqualTo(expectedHtml);
    }

    @Test
    public void isNonTranslatableNodeRecognised() {
        String html = "<div id='code-raw' translate='no'></div>";
        assertThat(ArticleUtil.isNonTranslatableNode(html)).isTrue();
    }

    @Test
    public void translatableNodeRecognised() {
        String html = "<div id='code-raw' translate='yes'></div>";
        assertThat(ArticleUtil.isNonTranslatableNode(html)).isFalse();
    }

    @Test
    public void translatableNodeRecognised2() {
        String html = "<div id='code-raw'></div>";
        assertThat(ArticleUtil.isNonTranslatableNode(html)).isFalse();
    }
}
