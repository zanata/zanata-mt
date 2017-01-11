package org.zanata.mt.article.kcs;

import java.util.List;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Locale;
import org.zanata.mt.service.PersistentTranslationService;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DomUtilTest {
    @Test
    public void testGetRawCodePreElements() {
        String html =
                "<html><div class=\"code-raw testing1 classes\"><span></span><pre></pre></div><div><div class=\"code-raw testing classes\"></div></div></html>";
        Document document = Jsoup.parse(html);
        Elements elements = DomUtil.getRawCodePreElements(document);
        assertThat(elements.size()).isEqualTo(1);
    }

    @Test
    public void testGetNonTranslatableNode() {
        String id = "testing-id";
        String expectedId = DomUtil.generateNodeId(id);
        Element nonTranslatableNode = DomUtil.generateNonTranslatableNode(id);
        assertThat(nonTranslatableNode.id()).isEqualTo(expectedId);
        assertThat(nonTranslatableNode.tagName()).isEqualTo("meta");
        assertThat(nonTranslatableNode.attr("translate")).isEqualTo("no");
    }

    @Test
    public void testIsPrivateNote() {
        String id = "private-notes-testing";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(DomUtil.isPrivateNotes(element)).isTrue();
    }

    @Test
    public void testIsNotPrivateNote() {
        String id = "testing-private-notes";
        Attributes attributes = new Attributes();
        attributes.put("id", id);
        Element element = new Element(Tag.valueOf("div"), "", attributes);
        assertThat(DomUtil.isPrivateNotes(element)).isFalse();
    }

    /**
     * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
     */
    @RunWith(MockitoJUnitRunner.class)
    public static class KCSArticleTypeServiceTest {

        private KCSArticleTypeService kcsArticleTypeService;

        @Mock
        private LocaleDAO localeDAO;

        @Mock
        private DocumentDAO documentDAO;

        @Mock
        private PersistentTranslationService persistentTranslationService;

        @Before
        public void setup() {
            kcsArticleTypeService = new KCSArticleTypeService(
                persistentTranslationService);
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
        public void testTranslate()
            throws BadRequestException {
            String divContent = getSampleArticleBody();
            Article article = new Article("Article title", divContent,
                "http://localhost:8080", ArticleType.KCS_ARTICLE.getType());
            Locale srcLocale = new Locale(LocaleId.EN, "English");
            Locale transLocale = new Locale(LocaleId.DE, "German");
            org.zanata.mt.model.Document doc = new org.zanata.mt.model.Document();
            List<String> headers = Lists.newArrayList(headerH1, headerContent);
            String translatedTitle = "Translated article title";
            String translatedHeaderH1 = "<h1 class=\"title\">Translated article header title</h1>";
            String translatedHeaderContent =
                "<span class=\"status inprogress\" data-content=" +
                    "\"Translated this solution is in progress.\">Translated Solution In Progress</span>";
            List<String> translatedHeaders =
                Lists.newArrayList(translatedHeaderH1, translatedHeaderContent);

            when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
            when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);
            when(documentDAO.getOrCreateByUrl(article.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

            when(persistentTranslationService
                .translate(article.getTitleText(), srcLocale,
                    transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                            .thenReturn(translatedTitle);
            when(persistentTranslationService.translate(headers, srcLocale,
                    transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                            .thenReturn(translatedHeaders);

            List<String> translatedSection1 =
                Lists.newArrayList("<h2>Translated Normal section</h2>",
                    "<p>Translated this is normal section, should be translated</p>");
            when(persistentTranslationService.translate(formattedSection1, srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE)).thenReturn(translatedSection1);

            List<String> translatedSection2 =
                    Lists.newArrayList("<h2>Translated coding section</h2>",
                            "<div class=\"code-raw\">\n <meta id=\"ZanataMT-0\" translate=\"no\">\n</div>",
                            "<div class=\"code-raw\">\n <meta id=\"ZanataMT-1\" translate=\"no\">\n</div>");
            when(persistentTranslationService.translate(formattedSection2, srcLocale,
                    transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE))
                            .thenReturn(translatedSection2);

            Article translateArticle = kcsArticleTypeService
                .translateArticle(article, srcLocale,
                transLocale, BackendID.MS);

            assertThat(translateArticle.getTitleText()).isEqualTo(translatedTitle);
            assertThat(translateArticle.getContentHTML())
                .contains(translatedSection1);
            assertThat(translateArticle.getContentHTML())
                .contains(translatedSection2.get(0));
            assertThat(translateArticle.getContentHTML())
                .doesNotContain(translatedSection2.get(1));

            assertThat(DTOUtil
                .removeWhiteSpaceBetweenTag(translateArticle.getContentHTML()))
                .contains(section3);

            // title
            verify(persistentTranslationService).translate(article.getTitleText(), srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE);

            // translateArticleHeader
            verify(persistentTranslationService).translate(headers, srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE);

            // translateArticleBody
            verify(persistentTranslationService).translate(formattedSection1,
                srcLocale, transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE);

            verify(persistentTranslationService).translate(formattedSection2,
                srcLocale, transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE);

            verifyNoMoreInteractions(persistentTranslationService);
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
}
