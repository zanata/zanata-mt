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
import org.mockito.runners.MockitoJUnitRunner;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.RawArticle;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.ArticleTranslatorService;
import org.zanata.mt.util.DTOUtil;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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
                articleTranslatorResource.translate(article, LocaleId.DE);
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

        // article with content but no url
        article = new Article(Lists.newArrayList(new TypeString("test",
                MediaType.TEXT_PLAIN)), null, "en");
        response = articleTranslatorResource.translate(article, LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
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
                new TypeString(htmls.get(0), MediaType.TEXT_HTML),
                new TypeString(htmls.get(1), MediaType.TEXT_HTML),
                new TypeString(text.get(0), MediaType.TEXT_PLAIN),
                new TypeString(text.get(1), MediaType.TEXT_PLAIN),
                new TypeString(htmls.get(2), MediaType.TEXT_HTML));

        List<TypeString> translatedContents = Lists.newArrayList(
                new TypeString(translatedHtmls.get(0), MediaType.TEXT_HTML),
                new TypeString(translatedHtmls.get(1), MediaType.TEXT_HTML),
                new TypeString(translatedText.get(0), MediaType.TEXT_PLAIN),
                new TypeString(translatedText.get(1), MediaType.TEXT_PLAIN),
                new TypeString(translatedHtmls.get(2), MediaType.TEXT_HTML));

        Article article = new Article(contents, "http://localhost", "en");
        Article translatedArticle =
                new Article(translatedContents, "http://localhost",
                        transLocale.getLocaleId().getId());

        System.out.println(DTOUtil.toJSON(translatedArticle));

        Document doc = Mockito.mock(Document.class);

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
                .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(article.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

        when(articleTranslatorService.translateArticle(article, srcLocale,
                transLocale, BackendID.MS)).thenReturn(translatedArticle);

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
        Response response = articleTranslatorResource.translate(rawArticle, null);
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

        // article with content but no url
        rawArticle = new RawArticle("title", "content", null, null, null);
        response = articleTranslatorResource.translate(rawArticle, LocaleId.DE);
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
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
