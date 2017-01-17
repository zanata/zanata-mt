package org.zanata.mt.article.kcs;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.article.ArticleContents;
import org.zanata.mt.article.ArticleNode;
import org.zanata.mt.model.BackendID;

import java.util.List;

import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    private String section1 =
        "<section><h2>Normal section</h2><p>This is normal section, should be translated</p></section>";
    private List<String> formattedSection1 =
        Lists.newArrayList("<h2>Normal section</h2>",
            "<p>This is normal section, should be translated</p>");

    private String section2 =
        "<section><h2>Coding section</h2><div class=\"code-raw\"><pre>source code1, stack trace here. Should not be translated</pre></div>"
            +
            "<div class=\"code-raw\"><pre>source code2, stack trace here. Should not be translated</pre></div></section>";
    private List<String> formattedSection2 =
        Lists.newArrayList("<h2>Coding section</h2>",
            "<div class=\"code-raw\">\n <meta id=\"ZanataMT-0\" translate=\"no\">\n</div>", "<div class=\"code-raw\">\n <meta id=\"ZanataMT-1\" translate=\"no\">\n</div>");

    private String section3 =
        "<section id=\"private-notes-section\"><h2>private section</h2><p>private notes which should be ignore</p></section>";

    @Test
    public void testTranslate() {
        String divContent = getSampleArticleBody();

        ArticleContents content = converter.extractArticle(divContent);

        List<ArticleNode> nodes = content.getArticleNodes();

        assertThat(content.getIgnoreNodeMap()).isNotEmpty();
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
