package org.zanata.mt.api;

import java.util.List;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.exception.BadTranslationRequestException;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.service.TranslationService;
import org.zanata.mt.util.DTOUtil;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TranslateResourceTest {

    private TranslateResource translateResource;

    @Mock
    private TranslationService translationService;

    @Mock
    private LocaleDAO localeDAO;

    @Mock
    private DocumentDAO documentDAO;

    @Before
    public void setup() {
        translateResource =
                new TranslateResource(translationService, localeDAO,
                        documentDAO);
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
            "<div class=\"code-raw\">\n <meta id=\"0\" translate=\"no\">\n</div>", "<div class=\"code-raw\">\n <meta id=\"1\" translate=\"no\">\n</div>");

    private String section3 =
            "<section id=\"private-notes-section\"><h2>private section</h2><p>private notes which should be ignore</p></section>";

    @Test
    public void testTranslateBadParams() {
        // empty source locale
        Article article = new Article(null, null, null);
        Response response = translateResource.translate(article, null,
                LocaleId.DE, Provider.MS.name());
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty trans locale
        response = translateResource.translate(article, LocaleId.EN,
            null, Provider.MS.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty provider
        response = translateResource.translate(article, LocaleId.EN,
            LocaleId.DE, null);
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // invalid provider
        response = translateResource.translate(article, LocaleId.EN,
            LocaleId.DE, "not supported provider");
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslateBadArticle() {
        // null article
        Article article = null;
        Response response = translateResource.translate(article, null,
            LocaleId.DE, Provider.MS.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with no content
        article = new Article(null, null, null);
        response = translateResource.translate(article, LocaleId.EN,
            LocaleId.DE, Provider.MS.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getEntity()).isEqualTo(article);

        // article with content but no url
        article = new Article("title", "content", null);
        response = translateResource.translate(article, null,
            LocaleId.DE, Provider.MS.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslate()
        throws TranslationEngineException, BadTranslationRequestException {
        String divContent = getSampleArticleBody();
        Article article = new Article("Article title", divContent,
                "http://localhost:8080");
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        Document doc = new Document();
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

        when(translationService.translate(article.getTitle(), srcLocale,
            transLocale, Provider.MS)).thenReturn(translatedTitle);
        when(translationService.translate(headers, srcLocale,
            transLocale, Provider.MS)).thenReturn(translatedHeaders);

        List<String> translatedSection1 =
            Lists.newArrayList("<h2>Translated Normal section</h2>",
                "<p>Translated this is normal section, should be translated</p>");
        when(translationService.translate(formattedSection1, srcLocale,
            transLocale, Provider.MS)).thenReturn(translatedSection1);

        List<String> translatedSection2 =
            Lists.newArrayList("<h2>Translated coding section</h2>",
                "<div class=\"code-raw\">\n <meta id=\"0\" translate=\"no\">\n</div>", "<div class=\"code-raw\">\n <meta id=\"1\" translate=\"no\">\n</div>");
        when(translationService.translate(formattedSection2, srcLocale,
            transLocale, Provider.MS)).thenReturn(translatedSection2);

        Response response =
                translateResource.translate(article, srcLocale.getLocaleId(),
                        transLocale.getLocaleId(), Provider.MS.name());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Article returnedArticle = (Article)response.getEntity();

        assertThat(returnedArticle.getTitle()).isEqualTo(translatedTitle);
        assertThat(returnedArticle.getDivContent())
                .contains(translatedSection1);
        assertThat(returnedArticle.getDivContent())
                .contains(translatedSection2.get(0));
        assertThat(returnedArticle.getDivContent())
                .doesNotContain(translatedSection2.get(1));

        assertThat(DTOUtil
                .removeWhiteSpaceBetweenTag(returnedArticle.getDivContent()))
                        .contains(section3);

        // title
        verify(translationService).translate(article.getTitle(), srcLocale,
                transLocale, Provider.MS);

        // translateArticleHeader
        verify(translationService).translate(headers, srcLocale,
            transLocale, Provider.MS);

        // translateArticleBody
        verify(translationService).translate(formattedSection1,
            srcLocale, transLocale, Provider.MS);

        verify(translationService).translate(formattedSection2,
            srcLocale, transLocale, Provider.MS);

        verifyNoMoreInteractions(translationService);

        doc.incrementCount();
        verify(documentDAO).persist(doc);
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
