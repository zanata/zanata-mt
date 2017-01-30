package org.zanata.mt.api;

import javax.ws.rs.BadRequestException;
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
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.ArticleTranslatorService;


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
    public void testTranslateBadParams() {
        // empty source locale
        Article article = new Article(null, null, null, null, null);
        Response response = articleTranslatorResource.translate(article,
                LocaleId.DE);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty trans locale
        response = articleTranslatorResource.translate(article, null);
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty backend
        response =
                articleTranslatorResource.translate(article, LocaleId.DE);
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

        // article with no content
        article = new Article(null, null, null, null, null);
        response = articleTranslatorResource.translate(article, LocaleId.DE);
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with content but no url
        article = new Article("title", "content", null, null, null);
        response = articleTranslatorResource.translate(article, LocaleId.DE);
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslate()
        throws BadRequestException {
        String divContent = "<div>content</div>";
        Article article = new Article("Article title", divContent,
                "http://localhost:8080", ArticleType.KCS_ARTICLE.getType(),
                LocaleId.EN.getId());
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        Document doc = new Document();
        String translatedTitle = "Translated article title";
        String translatedContent = "<div>translated content</div>";

        Article translatedArticle =
                new Article(translatedTitle, translatedContent,
                        article.getUrl(), ArticleType.KCS_ARTICLE.getType(),
                        transLocale.getLocaleId().getId());

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
            .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(article.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

        when(articleTranslatorService.translateArticle(article, srcLocale,
            transLocale, BackendID.MS)).thenReturn(translatedArticle);

        Response response =
                articleTranslatorResource.translate(article,
                        transLocale.getLocaleId());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Article returnedArticle = (Article)response.getEntity();

        assertThat(returnedArticle.getTitleText()).isEqualTo(translatedTitle);
        assertThat(returnedArticle.getContentHTML()).isEqualTo(translatedContent);
        assertThat(returnedArticle.getLocale())
                .isEqualTo(transLocale.getLocaleId().getId());

        doc.incrementUsedCount();
        verify(documentDAO).persist(doc);
    }
}
