package org.zanata.mt.api;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.RawArticle;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.ArticleTranslatorService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ArticleTranslatorResourceTest {

    private ArticleTranslatorResource articleTranslatorResource;

    @Mock
    private ArticleTranslatorService articleTranslatorService;

    @Mock
    private LocaleDAO localeDAO;

    @Mock
    private DocumentDAO documentDAO;

    @Before
    public void setup() {
        articleTranslatorResource =
                new ArticleTranslatorResource(articleTranslatorService, localeDAO, documentDAO);
    }

    @Test
    public void testConstructor() {
        ArticleTranslatorResource resource = new ArticleTranslatorResource();
    }

    @Test
    public void testTranslateArticleBadParams() {
        Article article = new Article(null, null, null);
        // empty trans locale
        Response response = articleTranslatorResource.translate(article, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslateBadArticle() {
        // null article
        Article article = null;
        Response response =
                articleTranslatorResource.translate(article, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty fields
        article = new Article(null, null, null);
        response = articleTranslatorResource.translate(article,
                LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with no content
        article = new Article(null, null, null);
        response = articleTranslatorResource.translate(article, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty locale
        article = new Article(
                Lists.newArrayList(new TypeString("string", "text/plain", "meta")),
                "http://localhost", null);
        response = articleTranslatorResource.translate(article, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with content but no url
        article = new Article(Lists.newArrayList(new TypeString("test",
                MediaType.TEXT_PLAIN, "meta")), null, "en");
        response = articleTranslatorResource.translate(article, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslateArticleBackendFailed() {
        testArticleFailed(new BadRequestException(), Response.Status.BAD_REQUEST);
    }

    @Test
    public void testTranslateArticleInternalError() {
        testArticleFailed(new ZanataMTException("error"), Response.Status.INTERNAL_SERVER_ERROR);
    }

    private void testArticleFailed(Exception expectedException,
            Response.Status expectedStatus) {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");

        List<TypeString> contents = Lists.newArrayList(
                new TypeString("<html>test</html>", MediaType.TEXT_HTML,
                        "meta"));
        Article article = new Article(contents, "http://localhost",
                srcLocale.getLocaleId().getId());

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);
        when(articleTranslatorService.isMediaTypeSupported(any()))
                .thenReturn(true);

        doThrow(expectedException).when(articleTranslatorService)
                .translateArticle(article, srcLocale,
                        transLocale, BackendID.MS);

        Response response =
                articleTranslatorResource
                        .translate(article, transLocale.getLocaleId());

        assertThat(response.getStatus())
                .isEqualTo(expectedStatus.getStatusCode());
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

        Article article = new Article(contents, "http://localhost", "en");
        Article translatedArticle =
                new Article(translatedContents, "http://localhost",
                        transLocale.getLocaleId().getId());

        Document doc = Mockito.mock(Document.class);

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(article.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

        when(articleTranslatorService.translateArticle(article, srcLocale,
                transLocale, BackendID.MS)).thenReturn(translatedArticle);
        when(articleTranslatorService.isMediaTypeSupported(any()))
                .thenReturn(true);

        Response response =
                articleTranslatorResource
                        .translate(article, transLocale.getLocaleId());

        assertThat(response.getStatus())
                    .isEqualTo(Response.Status.OK.getStatusCode());
        Article returnedArticle = (Article)response.getEntity();

        assertThat(returnedArticle.getContents()).isEqualTo(translatedContents);
        assertThat(returnedArticle.getLocale())
                .isEqualTo(transLocale.getLocaleId().getId());
        verify(doc).incrementUsedCount();
        verify(documentDAO).persist(doc);
    }

    @Test
    public void testTranslateRawArticleBadParams() {
        // empty trans locale
        RawArticle rawArticle = new RawArticle(null, null, null, null, null);
        Response response =
                articleTranslatorResource.translate(rawArticle, null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslateBadRawArticle() {
        // null article
        RawArticle rawArticle = null;
        Response response =
                articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with no content
        rawArticle = new RawArticle(null, null, null, null, null);
        response = articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // no article locale
        rawArticle =
                new RawArticle("title", "content", "http://localhost", null,
                        null);
        response = articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // no articleType
        rawArticle =
                new RawArticle("title", "content", "http://localhost", null,
                        LocaleId.EN.getId());
        response = articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // invalid articleType
        rawArticle =
                new RawArticle("title", "content", "http://localhost",
                        "NEW_ARTICLE", LocaleId.EN.getId());
        response = articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // no url
        rawArticle =
                new RawArticle("title", "content", null,
                        "KCS_ARTICLE", LocaleId.EN.getId());
        response = articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void testTranslateRawArticleBackendFailed() {
        testRawArticleFailed(new BadRequestException(), Response.Status.BAD_REQUEST);
    }

    @Test
    public void testTranslateRawArticleInternalError() {
        testRawArticleFailed(new ZanataMTException("failed"),
                Response.Status.INTERNAL_SERVER_ERROR);
    }

    private void testRawArticleFailed(Exception exceptionToThrow,
            Response.Status expectedStatus) {
        String divContent = "<div>content</div>";
        RawArticle rawArticle = new RawArticle("Article title", divContent,
                "http://localhost:8080", ArticleType.KCS_ARTICLE.getType(),
                LocaleId.EN.getId());
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);

        doThrow(exceptionToThrow).when(articleTranslatorService)
                .translateArticle(rawArticle, srcLocale,
                        transLocale, BackendID.MS);

        Response response =
                articleTranslatorResource
                        .translate(rawArticle, transLocale.getLocaleId());

        assertThat(response.getStatus())
                .isEqualTo(expectedStatus.getStatusCode());
    }

    @Test
    public void testTranslateRawArticle() throws BadRequestException {
        String divContent = "<div>content</div>";
        RawArticle rawArticle = new RawArticle("Article title", divContent,
                "http://localhost:8080", ArticleType.KCS_ARTICLE.getType(),
                LocaleId.EN.getId());
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        Document doc = new Document();
        String translatedTitle = "Translated article title";
        String translatedContent = "<div>translated content</div>";

        RawArticle translatedRawArticle =
                new RawArticle(translatedTitle, translatedContent,
                        rawArticle.getUrl(), ArticleType.KCS_ARTICLE.getType(),
                        transLocale.getLocaleId().getId());

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
            .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(rawArticle.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

        when(articleTranslatorService.translateArticle(rawArticle, srcLocale,
            transLocale, BackendID.MS)).thenReturn(translatedRawArticle);

        Response response =
                articleTranslatorResource.translate(rawArticle,
                        transLocale.getLocaleId());

        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());

        RawArticle returnedArticle = (RawArticle)response.getEntity();

        assertThat(returnedArticle.getTitleText()).isEqualTo(translatedTitle);
        assertThat(returnedArticle.getContentHTML()).isEqualTo(translatedContent);
        assertThat(returnedArticle.getLocale())
                .isEqualTo(transLocale.getLocaleId().getId());

        doc.incrementUsedCount();
        verify(documentDAO).persist(doc);
    }
}
