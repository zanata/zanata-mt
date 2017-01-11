package org.zanata.mt.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.ArticleConverter;
import org.zanata.mt.service.TranslationService;
import org.zanata.mt.util.TranslationUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class KCSArticleConverter implements ArticleConverter {

    private TranslationService translationService;

    @SuppressWarnings("unused")
    private KCSArticleConverter() {
    }

    @Inject
    public KCSArticleConverter(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public Article translateArticle(Article article, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        String translatedTitle =
                translationService.translate(article.getTitleText(), srcLocale,
                        transLocale, backendID, MediaType.TEXT_PLAIN_TYPE);

        String translatedContent =
                translateContent(article.getContentHTML(), srcLocale,
                        transLocale, backendID);

        return new Article(translatedTitle, translatedContent,
                article.getUrl(), ArticleType.KCS_ARTICLE.getType());
    }

    private String translateContent(String content, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        Document document = Jsoup.parse(content);
        document = translateArticleHeader(document, srcLocale, transLocale,
            backendID);
        document = translateArticleBody(document, srcLocale, transLocale,
            backendID);
        return TranslationUtil.extractBodyContentHTML(document);
    }

    private Document translateArticleHeader(Document document, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        Elements header = document.getElementsByTag("header");
        Element content = header.select("h1.title").first();
        Element secondaryContent = header.select("span.status").first();

        List<String> headers =
                Lists.newArrayList(content.outerHtml(),
                        secondaryContent.outerHtml());
        List<String> translations = translationService.translate(headers,
                srcLocale, transLocale, backendID, MediaType.TEXT_HTML_TYPE);

        if (!translations.isEmpty()) {
            Elements translatedContent =
                    TranslationUtil.parseAsElement(translations.get(0));
            if (translatedContent != null && !translatedContent.isEmpty()) {
                content.replaceWith(translatedContent.first());
            }

            if (translations.size() > 1) {
                Elements translatedSecondaryContent =
                        TranslationUtil.parseAsElement(translations.get(1));
                if (translatedSecondaryContent != null
                        && !translatedSecondaryContent.isEmpty()) {
                    secondaryContent
                            .replaceWith(translatedSecondaryContent.first());
                }
            }
        }
        return document;
    }

    private Document translateArticleBody(Document document, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        Elements sections = document.getElementsByTag("section");
        for (Element section : sections) {
            // section with id 'private-notes...' is non-translatable
            if (TranslationUtil.isPrivateNotes(section)) {
                continue;
            }
            translateSection(section, srcLocale, transLocale, backendID);
        }
        return document;
    }

    private Element translateSection(Element section, Locale srcLocale,
            Locale transLocale, BackendID backendID) throws BadRequestException,
            ZanataMTException {
        Map<String, Element> ignoreTranslationMap = Maps.newHashMap();

        /**
         * replace pre element with non-translatable node as placeholder
         */
        Elements codeElements = TranslationUtil.getRawCodePreElements(section);
        if (!codeElements.isEmpty()) {
            for (Element element : codeElements) {
                String id = String.valueOf(codeElements.indexOf(element));
                ignoreTranslationMap.put(id, element.clone());
                element.replaceWith(TranslationUtil.generateNonTranslatableNode(id));
            }
        }
        List<String> strings =
                section.children().stream().map(Element::outerHtml)
                        .collect(Collectors.toList());

        // send section to translate
        List<String> translations = translationService.translate(strings,
                srcLocale, transLocale, backendID, MediaType.TEXT_HTML_TYPE);
        if (!translations.isEmpty()) {
            int index = 0;
            for (Element sectionChild : section.children()) {
                sectionChild.replaceWith(
                        TranslationUtil.parseAsElement(translations.get(index))
                                .first());
                index++;
            }
        }
        // replace placeholder with initial element
        for (Map.Entry<String, Element> entry : ignoreTranslationMap
                .entrySet()) {
            String id = TranslationUtil.generateNodeId(entry.getKey());
            Element placeholderElement = section.getElementById(id);
            assert placeholderElement != null;

            placeholderElement.replaceWith(entry.getValue());
        }
        return section;
    }
}
