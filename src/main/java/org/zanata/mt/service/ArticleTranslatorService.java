package org.zanata.mt.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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

        iterateBoth(articleContents.getArticleNodes(), translations,
                (node, translation) -> {
                    node.setHtml(translation);
                });

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

    private ArticleConverter getConverter(Article article)
        throws ZanataMTException {
        ArticleType articleType = new ArticleType(article.getArticleType());

        if (articleType.equals(ArticleType.KCS_ARTICLE)) {
            return new KCSArticleConverter();
        }
        throw new ZanataMTException("Not supported articleType" + articleType);
    }

    private static <T1, T2> void iterateBoth(Iterable<T1> c1, Iterable<T2> c2,
            BiConsumer<T1, T2> consumer) {
        Iterator<T1> i1 = c1.iterator();
        Iterator<T2> i2 = c2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            consumer.accept(i1.next(), i2.next());
        }
        assert !i1.hasNext();
        assert !i2.hasNext();
    }
}
