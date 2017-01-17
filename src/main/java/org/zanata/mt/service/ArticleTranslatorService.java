package org.zanata.mt.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.zanata.mt.api.dto.Article;
import org.zanata.mt.article.ArticleContents;
import org.zanata.mt.article.ArticleNode;
import org.zanata.mt.article.kcs.KCSArticleConverter;
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

    private PersistentTranslationService persistentTranslationService;

    @SuppressWarnings("unused")
    public ArticleTranslatorService() {
    }

    @Inject
    public ArticleTranslatorService(
            PersistentTranslationService persistentTranslationService) {
        this.persistentTranslationService = persistentTranslationService;
    }

    public Article translateArticle(Article article, Locale srcLocale,
        Locale transLocale, BackendID backendID) throws BadRequestException,
        ZanataMTException {
        ArticleConverter converter = getConverter(article);

        String translatedPageTitle =
            persistentTranslationService
                .translate(article.getTitleText(), srcLocale,
                    transLocale, backendID, MediaType.TEXT_PLAIN_TYPE);

        ArticleContents articleContents =
                converter.extractArticle(article.getContentHTML());

        List<String> htmls = articleContents.getArticleNodes().stream()
                .map(ArticleNode::getHtml).collect(
                        Collectors.toList());

        List<String> translations =
                persistentTranslationService.translate(htmls,
                        srcLocale, transLocale, backendID,
                        MediaType.TEXT_HTML_TYPE);

        assert htmls.size() == translations.size();

        int index = 0;
        for (ArticleNode articleNode: articleContents.getArticleNodes()) {
            articleNode.setHtml(translations.get(index));
            index++;
        }

        // replace placeholder with initial element
        Map<String, ArticleNode> ignoreNodeMap = articleContents.getIgnoreNodeMap();
        if (ignoreNodeMap != null && !ignoreNodeMap.isEmpty()) {
            for (Map.Entry<String, ArticleNode> entry : ignoreNodeMap
                    .entrySet()) {
                articleContents.replaceNodeByName(entry.getKey(),
                        entry.getValue());
            }
        }

        return new Article(translatedPageTitle,
                articleContents.getDocumentHtml(), article.getUrl(),
                article.getArticleType());
    }

//    public Article translateArticle(Article article, Locale srcLocale,
//            Locale transLocale, BackendID backendID) throws BadRequestException,
//            ZanataMTException {
//        ArticleTypeService articleTypeService = getArticleTypeService(article);
//
//        return articleTypeService.translateArticle(article, srcLocale,
//                transLocale, backendID);
//    }
//
//    private ArticleTypeService getArticleTypeService(Article article)
//        throws ZanataMTException {
//        ArticleType articleType = new ArticleType(article.getArticleType());
//
//        if (articleType.equals(ArticleType.KCS_ARTICLE)) {
//           return kcsArticleTypeService;
//        }
//        throw new ZanataMTException("Not supported articleType" + articleType);
//    }

    private ArticleConverter getConverter(Article article)
        throws ZanataMTException {
        ArticleType articleType = new ArticleType(article.getArticleType());

        if (articleType.equals(ArticleType.KCS_ARTICLE)) {
            return new KCSArticleConverter();
        }
        throw new ZanataMTException("Not supported articleType" + articleType);
    }
}
