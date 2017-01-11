package org.zanata.mt.service;

import javax.ws.rs.BadRequestException;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Locale;

/**
 * Interface for different Article type service.
 *
 * Implementation of this should persist any new translation got back from
 * backend service.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ArticleTypeService {

    /**
     * Translate article by looking at existing translations + triggering
     * backend MT service.
     *
     * @param article
     * @param srcLocale
     * @param transLocale
     * @param backendID
     * @return
     * @throws BadRequestException
     * @throws ZanataMTException
     */
    Article translateArticle(Article article, Locale srcLocale,
        Locale transLocale, BackendID backendID) throws BadRequestException,
        ZanataMTException;
}
