package org.zanata.mt.service;

import javax.ws.rs.BadRequestException;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ResourceService {

    Article translateArticle(Article article, Locale srcLocale,
        Locale transLocale, Provider provider) throws BadRequestException,
        ZanataMTException;
}
