package org.zanata.mt.service;

import javax.ws.rs.BadRequestException;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Locale;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ArticleConverter {

    Article translateArticle(Article article, Locale srcLocale,
        Locale transLocale, BackendID backendID) throws BadRequestException,
        ZanataMTException;
}
