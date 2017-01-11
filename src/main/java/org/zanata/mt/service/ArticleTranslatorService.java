package org.zanata.mt.service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;

/**
 * Translate an article using service based on {@link Article#getArticleType()}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class ArticleTranslatorService {

    private ArticleTypeService kcsArticleTypeService;

    @SuppressWarnings("unused")
    public ArticleTranslatorService() {
    }

    @Inject
    public ArticleTranslatorService(ArticleTypeService kcsArticleTypeService) {
        this.kcsArticleTypeService = kcsArticleTypeService;
    }

    public Article translateArticle(Article article, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        ArticleTypeService articleTypeService = getArticleTypeService(article);

        return articleTypeService.translateArticle(article, srcLocale,
                transLocale, backendID);
    }

    private ArticleTypeService getArticleTypeService(Article article)
        throws ZanataMTException {
        ArticleType articleType = new ArticleType(article.getArticleType());

        if (articleType.equals(ArticleType.KCS_ARTICLE)) {
           return kcsArticleTypeService;
        }
        throw new ZanataMTException("Not supported articleType" + articleType);
    }
}
