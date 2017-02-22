package org.zanata.mt.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.api.dto.RawArticle;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.article.ArticleContents;
import org.zanata.mt.article.ArticleNode;
import org.zanata.mt.article.kcs.KCSArticleConverter;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;

import com.google.common.collect.Lists;

import static java.util.stream.Collectors.toList;

/**
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

    /**
     * Translate an Article
     * {@link Article}
     **/
    public Article translateArticle(Article article, Locale srcLocale,
            Locale transLocale, BackendID backendID)
            throws BadRequestException, ZanataMTException {

        LinkedHashMap<Integer, TypeString> indexHTMLMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, TypeString> indexTextMap = new LinkedHashMap<>();

        //group by media type and send in batch for translation
        int index = 0;
        for (TypeString typeString: article.getContents()) {
            MediaType mediaType = getMediaType(typeString.getType());

            if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                indexHTMLMap.put(index, typeString);
            } else if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                indexTextMap.put(index, typeString);
            }
            index++;
        }

        List<TypeString> results = Lists.newArrayList(article.getContents());

        if (!indexHTMLMap.isEmpty()) {
            List<String> htmls =
                    indexHTMLMap.values().stream().map(TypeString::getValue)
                            .collect(toList());

            List<String> translatedHtmls = persistentTranslationService
                    .translate(htmls, srcLocale, transLocale, backendID,
                            MediaType.TEXT_HTML_TYPE);

            transferToResults(translatedHtmls, indexHTMLMap, results,
                    MediaType.TEXT_HTML);
        }

        if (!indexTextMap.isEmpty()) {
            List<String> strings =
                    indexTextMap.values().stream().map(TypeString::getValue)
                            .collect(toList());

            List<String> translatedStrings = persistentTranslationService
                    .translate(strings, srcLocale, transLocale, backendID,
                            MediaType.TEXT_PLAIN_TYPE);

            transferToResults(translatedStrings, indexTextMap, results,
                    MediaType.TEXT_PLAIN);
        }

        return new Article(results, article.getUrl(),
                transLocale.getLocaleId().getId(), backendID.getId());
    }

    /**
     * Translate an RawArticle
     * {@link RawArticle}
     **/
    public RawArticle translateArticle(RawArticle rawArticle, Locale srcLocale,
            Locale transLocale, BackendID backendID)
            throws BadRequestException, ZanataMTException {
        ArticleConverter converter = getConverter(rawArticle);

        String translatedPageTitle =
                persistentTranslationService
                        .translate(rawArticle.getTitleText(), srcLocale,
                                transLocale, backendID,
                                MediaType.TEXT_PLAIN_TYPE);

        ArticleContents articleContents =
                converter.extractArticle(rawArticle.getContentHTML());

        List<String> translatableHtmls = articleContents.getArticleNodes()
                .stream()
                .map(ArticleNode::getHtml)
                .collect(toList());

        List<String> translatedHtmls =
                persistentTranslationService.translate(translatableHtmls,
                        srcLocale, transLocale, backendID,
                        MediaType.TEXT_HTML_TYPE);

        assert translatableHtmls.size() == translatedHtmls.size();
        assert articleContents.getArticleNodes().size() == translatedHtmls.size();

        forBoth(articleContents.getArticleNodes(),
                translatedHtmls,
                ArticleNode::setHtml);

        articleContents.replacePlaceholdersWithOriginals();

        return new RawArticle(translatedPageTitle,
                articleContents.getDocumentHtml(), rawArticle.getUrl(),
                rawArticle.getArticleType(), transLocale.getLocaleId().getId(),
                backendID.getId());
    }

    // insert translatedStrings into results by index in indexStringMap
    private void transferToResults(List<String> translatedStrings,
            LinkedHashMap<Integer, TypeString> indexStringMap,
            List<TypeString> results, String mediaType) {
        int index = 0;
        for (String translatedString : translatedStrings) {
            Map.Entry<Integer, TypeString>
                    entry = Iterables.get(indexStringMap.entrySet(), index);
            results.set(entry.getKey(),
                    new TypeString(translatedString, mediaType,
                            entry.getValue().getMetadata()));
            index++;
        }
    }

    public MediaType getMediaType(String mediaType) throws BadRequestException {
        if (isMediaTypeSupported(mediaType)) {
            return MediaType.valueOf(mediaType);
        }
        throw new BadRequestException("Unsupported media type:" + mediaType);
    }

    public boolean isMediaTypeSupported(String mediaType) {
        if (StringUtils.equalsAny(mediaType, MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN)) {
            return true;
        }
        return false;
    }

    public ArticleConverter getConverter(RawArticle rawArticle)
        throws ZanataMTException {
        ArticleType articleType = new ArticleType(rawArticle.getArticleType());

        if (articleType.equals(ArticleType.KCS_ARTICLE)) {
            return new KCSArticleConverter();
        }
        throw new ZanataMTException("Not supported articleType" + articleType);
    }

    // Assumes that both iterables are the same size.
    private static <T1, T2> void forBoth(Iterable<T1> c1, Iterable<T2> c2,
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
