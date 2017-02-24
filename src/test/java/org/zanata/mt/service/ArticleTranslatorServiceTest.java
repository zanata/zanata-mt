package org.zanata.mt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.RawArticle;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.article.kcs.KCSArticleConverter;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Locale;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ArticleTranslatorServiceTest {

    private ArticleTranslatorService articleTranslatorService;

    @Mock
    private PersistentTranslationService persistentTranslationService;

    @Before
    public void setup() {
        articleTranslatorService =
                new ArticleTranslatorService(persistentTranslationService);
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
    private String section2Content1 = "<div class=\"code-raw\"><pre>source code1, stack trace here. Should not be translated</pre></div>";
    private String section2Content2 = "<div class=\"code-raw\"><pre>source code2, stack trace here. Should not be translated</pre></div>";
    private String section2 =
        "<section>" + section2Header + section2Content1 + section2Content2 + "</section>";

    private String processedSection2Content1 = "<div class=\"code-raw\">\n <meta name=\"ZanataMT-0\" translate=\"no\">\n</div>";
    private String processedSection2Content2 = "<div class=\"code-raw\">\n <meta name=\"ZanataMT-1\" translate=\"no\">\n</div>";

    private String section3Header = "<h2>private section</h2>";
    private String section3Content = "<p>private notes which should be ignore</p>";
    private String section3 =
        "<section id=\"private-notes-section\">" + section3Header + section3Content + "</section>";

    @Test
    public void testEmptyConstructor() {
        ArticleTranslatorService articleTranslatorService = new ArticleTranslatorService();
    }

    @Test
    public void testGetMediaType() {
        assertThatThrownBy(
                () -> articleTranslatorService.getMediaType("notSupportType"))
                .isInstanceOf(BadRequestException.class);

        String mediaType = "text/plain";
        assertThat(articleTranslatorService.getMediaType(mediaType)).isNotNull()
                .isEqualTo(MediaType.TEXT_PLAIN_TYPE);

        mediaType = "text/html";
        assertThat(articleTranslatorService.getMediaType(mediaType)).isNotNull()
                .isEqualTo(MediaType.TEXT_HTML_TYPE);
    }

    @Test
    public void testGetConverter() {
        RawArticle emptyTypeRawArticle =
                new RawArticle("titleText", "htmlContent", "http://localhost",
                        "", "en");
        assertThatThrownBy(
                () -> articleTranslatorService.getConverter(emptyTypeRawArticle))
                .isInstanceOf(ZanataMTException.class);

        RawArticle invalidRawArticle =
                new RawArticle("titleText", "htmlContent", "http://localhost",
                        "invalidType", "en");
        assertThatThrownBy(
                () -> articleTranslatorService.getConverter(invalidRawArticle))
                .isInstanceOf(ZanataMTException.class);

        RawArticle validRawArticle =
                new RawArticle("titleText", "htmlContent", "http://localhost",
                        ArticleType.KCS_ARTICLE.getType(), "en");
        assertThat(articleTranslatorService.getConverter(validRawArticle)).isNotNull()
                .isInstanceOf(KCSArticleConverter.class);
    }

    @Test
    public void testTranslateRaw() throws BadRequestException {
        String divContent = getSampleArticleBody();
        RawArticle article = new RawArticle("Article title", divContent,
            "http://localhost:8080", ArticleType.KCS_ARTICLE.getType(), LocaleId.EN_US.getId());
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        org.zanata.mt.model.Document doc = new org.zanata.mt.model.Document();

        String translatedTitle = "Translated article title";
        String translatedHeaderH1 = "<h1 class=\"title\">Translated article header title</h1>";
        String translatedHeaderContent =
            "<span class=\"status inprogress\" data-content=" +
                "\"Translated this solution is in progress.\">Translated Solution In Progress</span>";
        String translatedSection1Header = "<h2>Translated Normal section</h2>";
        String translatedSection1Content = "<p>Translated this is normal section, should be translated</p>";
        String translatedSection2Header = "<h2>Translated coding section</h2>";
        String translatedSection2Content1 = "<div class=\"code-raw\">\n <meta name=\"ZanataMT-0\" translate=\"no\">\n</div>";
        String translatedSection2Content2 = "<div class=\"code-raw\">\n <meta name=\"ZanataMT-1\" translate=\"no\">\n</div>";

        List<String> translatedStrings =
                Lists.newArrayList(translatedHeaderH1, translatedHeaderContent,
                        translatedSection1Header, translatedSection1Content,
                        translatedSection2Header, translatedSection2Content1,
                        translatedSection2Content2);

        when(persistentTranslationService.translate(article.getTitleText(), srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                        .thenReturn(translatedTitle);

        List<String> requestTranslations = Lists.newArrayList(headerH1,
            headerContent, section1Header, section1Content, section2Header,
            processedSection2Content1, processedSection2Content2);

        when(persistentTranslationService.translate(requestTranslations,
                srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE))
                        .thenReturn(translatedStrings);

        RawArticle translatedArticle =
                articleTranslatorService.translateRawArticle(article, srcLocale,
                        transLocale, BackendID.MS);

        assertThat(translatedArticle.getTitleText()).isEqualTo(translatedTitle);

        assertThat(translatedArticle.getContentHTML())
                .containsSequence(translatedHeaderH1, translatedHeaderContent,
                        translatedSection1Header, translatedSection1Content,
                        translatedSection2Header)
                .doesNotContain(processedSection2Content1)
                .doesNotContain(processedSection2Content2);

        assertThat(DTOUtil
            .removeWhiteSpaceBetweenTag(translatedArticle.getContentHTML()))
            .contains(section3);
    }

    @Test
    public void testTranslateArticle() {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");

        List<String> htmls =
                Lists.newArrayList("<html><body>Entry 1</body></html>",
                        "<html><body>Entry 2</body></html>",
                        "<html><body>Entry 5</body></html>");
        List<String> text = Lists.newArrayList("Entry 3", "Entry 4");

        List<String> translatedHtmls =
                Lists.newArrayList("<html><body>MS: Entry 1</body></html>",
                        "<html><body>MS: Entry 2</body></html>",
                        "<html><body>MS: Entry 5</body></html>");
        List<String> translatedText = Lists.newArrayList("MS: Entry 3", "MS: Entry 4");

        List<TypeString> contents = Lists.newArrayList(
                new TypeString(htmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(htmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(text.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(text.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(htmls.get(2), MediaType.TEXT_HTML, "meta5"));

        List<TypeString> translatedContents = Lists.newArrayList(
                new TypeString(translatedHtmls.get(0), MediaType.TEXT_HTML, "meta1"),
                new TypeString(translatedHtmls.get(1), MediaType.TEXT_HTML, "meta2"),
                new TypeString(translatedText.get(0), MediaType.TEXT_PLAIN, "meta3"),
                new TypeString(translatedText.get(1), MediaType.TEXT_PLAIN, "meta4"),
                new TypeString(translatedHtmls.get(2), MediaType.TEXT_HTML, "meta5"));

        DocumentContent
                article = new DocumentContent(contents, "http://localhost", "en");

        when(persistentTranslationService.translate(htmls,
                srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE))
                .thenReturn(translatedHtmls);

        when(persistentTranslationService.translate(text,
                srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                .thenReturn(translatedText);


        DocumentContent translatedDocContent = articleTranslatorService
                .translateDocument(article, srcLocale, transLocale,
                        BackendID.MS);

        assertThat(translatedDocContent.getLocale())
                .isEqualTo(transLocale.getLocaleId().getId());
        assertThat(translatedDocContent.getBackendId()).isEqualTo(BackendID.MS.getId());
        assertThat(translatedDocContent.getUrl()).isEqualTo(article.getUrl());
        assertThat(translatedDocContent.getContents())
                .isEqualTo(translatedContents);

        verify(persistentTranslationService)
                .translate(htmls, srcLocale, transLocale, BackendID.MS,
                        MediaType.TEXT_HTML_TYPE);

        verify(persistentTranslationService)
                .translate(text, srcLocale, transLocale, BackendID.MS,
                        MediaType.TEXT_PLAIN_TYPE);
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
