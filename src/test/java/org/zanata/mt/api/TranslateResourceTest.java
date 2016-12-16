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
import org.zanata.mt.model.ContentType;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.service.ResourceService;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class TranslateResourceTest {

    private TranslateResource translateResource;

    @Mock
    private ResourceService resourceService;

    @Mock
    private LocaleDAO localeDAO;

    @Mock
    private DocumentDAO documentDAO;

    @Before
    public void setup() {
        translateResource =
                new TranslateResource(resourceService, localeDAO, documentDAO);
    }

    @Test
    public void testTranslateBadParams() {
        // empty source locale
        Article article = new Article(null, null, null);
        Response response = translateResource.translate(article, null,
                LocaleId.DE, Provider.MS.name(), ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty trans locale
        response = translateResource.translate(article, LocaleId.EN,
            null, Provider.MS.name(), ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // empty provider
        response = translateResource.translate(article, LocaleId.EN,
            LocaleId.DE, null, ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // invalid provider
        response = translateResource.translate(article, LocaleId.EN,
            LocaleId.DE, "not supported provider", ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslateBadArticle() {
        // null article
        Article article = null;
        Response response = translateResource.translate(article, null,
            LocaleId.DE, Provider.MS.name(), ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with no content
        article = new Article(null, null, null);
        response = translateResource.translate(article, LocaleId.EN,
            LocaleId.DE, Provider.MS.name(), ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        // article with content but no url
        article = new Article("title", "content", null);
        response = translateResource.translate(article, null,
            LocaleId.DE, Provider.MS.name(), ContentType.KCS_ARTICLE.name());
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testTranslate()
        throws BadRequestException {
        String divContent = "<div>content</div>";
        Article article = new Article("Article title", divContent,
                "http://localhost:8080");
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        Document doc = new Document();
        String translatedTitle = "Translated article title";
        String translatedContent = "<div>translated content</div>";

        Article translatedArticle =
            new Article(translatedTitle, translatedContent, article.getUrl());

        when(localeDAO.getOrCreateByLocaleId(srcLocale.getLocaleId()))
                .thenReturn(srcLocale);
        when(localeDAO.getOrCreateByLocaleId(transLocale.getLocaleId()))
            .thenReturn(transLocale);
        when(documentDAO.getOrCreateByUrl(article.getUrl(), srcLocale,
                transLocale)).thenReturn(doc);

        when(resourceService.translateArticle(article, srcLocale,
            transLocale, Provider.MS)).thenReturn(translatedArticle);

        Response response =
            translateResource.translate(article, srcLocale.getLocaleId(),
                transLocale.getLocaleId(), Provider.MS.name(),
                ContentType.KCS_ARTICLE.name());

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        Article returnedArticle = (Article)response.getEntity();

        assertThat(returnedArticle.getTitle()).isEqualTo(translatedTitle);
        assertThat(returnedArticle.getContent()).isEqualTo(translatedContent);

        doc.incrementUsedCount();
        verify(documentDAO).persist(doc);
    }
}
