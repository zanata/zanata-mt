package org.zanata.mt.service;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.article.ArticleContents;

/**
 * Interface for different Article converter.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface ArticleConverter {

    /**
     * Convert article into {@link ArticleContents}
     *
     * @param article
     * @param srcLocale
     * @param transLocale
     * @param backendID
     */
    ArticleContents extractArticle(String html);
}
