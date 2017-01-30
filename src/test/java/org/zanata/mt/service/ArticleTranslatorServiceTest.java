package org.zanata.mt.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

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
import org.zanata.mt.model.Document;
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
    private LocaleDAO localeDAO;

    @Mock
    private DocumentDAO documentDAO;

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
    public void testTranslate() throws BadRequestException {
        String divContent = getSampleArticleBody();
        Article article = new Article("Article title", divContent,
            "http://localhost:8080", ArticleType.KCS_ARTICLE.getType(), LocaleId.EN_US.getId());
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        Document doc = new Document();

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

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
            .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
            .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(article.getUrl(), srcLocale,
            transLocale)).thenReturn(doc);

        when(persistentTranslationService.translate(article.getTitleText(), srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_PLAIN_TYPE))
                        .thenReturn(translatedTitle);

        List<String> requestTranslations = Lists.newArrayList(headerH1,
            headerContent, section1Header, section1Content, section2Header,
            processedSection2Content1, processedSection2Content2);

        when(persistentTranslationService.translate(requestTranslations, srcLocale,
                transLocale, BackendID.MS, MediaType.TEXT_HTML_TYPE))
                        .thenReturn(translatedStrings);

        Article translatedArticle =
                articleTranslatorService.translateArticle(article, srcLocale,
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
