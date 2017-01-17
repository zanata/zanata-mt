package org.zanata.mt.article.kcs;

import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.article.ArticleContents;
import org.zanata.mt.article.ArticleNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class KCSArticleConverterTest {

    private KCSArticleConverter converter;

    @Before
    public void setup() {
        converter = new KCSArticleConverter();
    }

    private String headerH1 = "<h1 class=\"title\">Article header title</h1>";

    private String headerContent =
        "<span class=\"status inprogress\" data-content=" +
            "\"This solution is in progress.\">Solution In Progress</span>";

    private String section1Header = "<h2>Normal section</h2>";
    private String section1Content = "<p>This is normal section, should be translated</p>";
    private String section1 =
        "<section>" + section1Header + section1Content + "</section>";

    private String section2Header = "<h2>Coding section</h2>";
    private String section2Content =
            "<div class=\"code-raw\"><pre>source code1, stack trace here. Should not be translated</pre></div>"
                    + "<div class=\"code-raw\"><pre>source code2, stack trace here. Should not be translated</pre></div>";
    private String section2 =
        "<section>" + section2Header + section2Content + "</section>";

    private String section3Header = "<h2>private section</h2>";
    private String section3Content = "<p>private notes which should be ignore</p>";
    private String section3 =
        "<section id=\"private-notes-section\">" + section3Header + section3Content + "</section>";

    @Test
    public void testExtractArticle() {
        String divContent = getSampleArticleBody();

        ArticleContents content = converter.extractArticle(divContent);
        List<ArticleNode> nodes = content.getArticleNodes();

        assertThat(nodes).extracting(ArticleNode::getHtml)
                .contains(headerH1, headerContent, section1Header,
                        section1Content, section2Header)
                .doesNotContain(section2Content, section3Header,
                        section3Content);

        assertThat(content.getIgnoreNodeMap()).isNotEmpty().hasSize(2);
    }

    private String getSampleArticleBody() {
        String html = "<header class='header'>" + headerH1 +
            "<div class='header-meta'>" + headerContent +
            " - Update<time class='moment_date' datetime='2014-08-12T02:11:46+10:00' title="
            + "'August 12 2014 at 2:11 AM'>August 12 2014 at 2:11 AM</time>"
            + "</div>" +
            "</header>" + section1 + section2 + section3;
        return html;
    }
}
