package org.zanata.mt.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Singleton;
import javax.inject.Inject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.exception.BadTranslationRequestException;
import org.zanata.mt.exception.TranslationProviderException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.service.ResourceService;
import org.zanata.mt.service.TranslationService;
import org.zanata.mt.util.TranslationUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Singleton
public class KCSResourceService implements ResourceService {

    @Inject
    private TranslationService translationService;

    public Article translateArticle(Article article, Locale srcLocale,
            Locale transLocale, Provider provider)
        throws BadTranslationRequestException, TranslationProviderException {
        String translatedTitle =
                translationService.translate(article.getTitle(), srcLocale,
                        transLocale, provider);

        String translatedContent =
                translateContent(article.getContent(), srcLocale,
                        transLocale, provider);

        return new Article(translatedTitle, translatedContent, article.getUrl());
    }

    private String translateContent(String content, Locale srcLocale,
        Locale transLocale, Provider provider)
        throws TranslationProviderException, BadTranslationRequestException {
        Document document = Jsoup.parse(content);
        document = translateArticleHeader(document, srcLocale, transLocale,
            provider);
        document = translateArticleBody(document, srcLocale, transLocale,
            provider);
        return TranslationUtil.getBodyHTMLContent(document);
    }

    private Document translateArticleHeader(Document document, Locale srcLocale,
        Locale transLocale, Provider provider)
        throws TranslationProviderException,
        BadTranslationRequestException {
        Elements header = document.getElementsByTag("header");
        Element content = header.select("h1.title").first();
        Element secondaryContent = header.select("span.status").first();

        List<String> headers =
            Lists.newArrayList(content.outerHtml(),
                secondaryContent.outerHtml());
        List<String> translations = translationService.translate(headers,
            srcLocale, transLocale, provider);

        if (!translations.isEmpty()) {
            content.replaceWith(
                TranslationUtil.parseAsElement(translations.get(0))
                    .first());
            if (translations.size() > 1) {
                secondaryContent.replaceWith(
                    TranslationUtil.parseAsElement(translations.get(1))
                        .first());
            }
        }
        return document;
    }

    private Document translateArticleBody(Document document, Locale srcLocale,
        Locale transLocale, Provider provider)
        throws TranslationProviderException, BadTranslationRequestException {
        Elements sections = document.getElementsByTag("section");
        for (Element section : sections) {
            // section with id 'private-notes...' is non-translatable
            if (TranslationUtil.isPrivateNotes(section)) {
                continue;
            }
            translateSection(section, srcLocale, transLocale, provider);
        }
        return document;
    }

    private Element translateSection(Element section, Locale srcLocale,
        Locale transLocale, Provider provider)
        throws TranslationProviderException, BadTranslationRequestException {
        Map<String, Element> ignoreTranslationMap = Maps.newHashMap();

        /**
         * replace pre element with non-translatable node as
         * placeholder
         */
        Elements codeElements = TranslationUtil.getRawCodePreElements(section);
        if (!codeElements.isEmpty()) {
            for (Element element: codeElements) {
                String id = String.valueOf(codeElements.indexOf(element));
                ignoreTranslationMap.put(id, element.clone());
                element.replaceWith(TranslationUtil.getNonTranslatableNode(id));
            }
        }
        List<String> strings =
            section.children().stream().map(Element::outerHtml)
                .collect(Collectors.toList());

        // send section to translate
        List<String> translations = translationService.translate(strings,
            srcLocale, transLocale, provider);
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
            Element placeholderElement =
                section.getElementsByAttributeValue("id",
                    entry.getKey()).get(0);
            placeholderElement.replaceWith(entry.getValue());
        }
        return section;
    }
}
