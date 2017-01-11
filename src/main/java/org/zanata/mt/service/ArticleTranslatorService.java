package org.zanata.mt.service;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class ArticleTranslatorService {

    @Inject
    private ArticleConverter kcsArticleService;
    
    public Article translateArticle(Article article, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        return kcsArticleService.translateArticle(article, srcLocale,
                transLocale, backendID);
    }
}
