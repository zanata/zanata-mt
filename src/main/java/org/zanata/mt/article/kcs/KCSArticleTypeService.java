package org.zanata.mt.article.kcs;

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
import org.zanata.mt.service.PersistentTranslationService;
import org.zanata.mt.util.DomUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * KCS Article type service class.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class KCSArticleTypeService {

    private PersistentTranslationService persistentTranslationService;

    @SuppressWarnings("unused")
    public KCSArticleTypeService() {
    }

//    @Inject
//    public KCSArticleTypeService(
//            PersistentTranslationService persistentTranslationService) {
//        this.persistentTranslationService = persistentTranslationService;
//    }
//
//    @Override
//    public Article translateArticle(Article article, Locale srcLocale,
//            Locale transLocale, BackendID backendID) throws BadRequestException,
//            ZanataMTException {
//        String translatedPageTitle =
//                persistentTranslationService
//                    .translate(article.getTitleText(), srcLocale,
//                        transLocale, backendID, MediaType.TEXT_PLAIN_TYPE);
//
//        String translatedContent =
//                translateContent(article.getContentHTML(), srcLocale,
//                        transLocale, backendID);
//
//        return new Article(translatedPageTitle, translatedContent,
//                article.getUrl(), ArticleType.KCS_ARTICLE.getType());
//    }
//
//    private String translateContent(String content, Locale srcLocale,
//            Locale transLocale, BackendID backendID) throws BadRequestException,
//            ZanataMTException {
//        Document document = Jsoup.parse(content);
//
//        translateArticleHeader(document, srcLocale, transLocale, backendID);
//        translateArticleBody(document, srcLocale, transLocale, backendID);
//        return DomUtil.extractBodyContentHTML(document);
//    }
//
//    private void translateArticleHeader(Document document, Locale srcLocale,
//            Locale transLocale, BackendID backendID) throws BadRequestException,
//            ZanataMTException {
//        Elements header = document.getElementsByTag("header");
//        Element solutionTitle = header.select("h1.title").first();
//        Element solutionStatus = header.select("span.status").first();
//
//        List<String> htmlHeaders =
//                Lists.newArrayList(solutionTitle.outerHtml(),
//                        solutionStatus.outerHtml());
//        List<String> translatedHtmlHeaders =
//                persistentTranslationService.translate(htmlHeaders,
//                        srcLocale, transLocale, backendID,
//                        MediaType.TEXT_HTML_TYPE);
//        //assert translatedHeaders size = 2
//
//        if (!translatedHtmlHeaders.isEmpty()) {
//            Elements translatedSolutionTitle =
//                DomUtil.parseAsElement(translatedHtmlHeaders.get(0));
//            if (translatedSolutionTitle != null && !translatedSolutionTitle.isEmpty()) {
//                //assert translatedSolutionTitle has 1 element
//                solutionTitle.replaceWith(translatedSolutionTitle.first());
//            }
//
//            if (translatedHtmlHeaders.size() > 1) {
//                Elements translatedSolutionStatus =
//                    DomUtil.parseAsElement(translatedHtmlHeaders.get(1));
//                if (translatedSolutionStatus != null
//                        && !translatedSolutionStatus.isEmpty()) {
//                    //assert translatedSolutionTitle has 1 element
//                    solutionStatus
//                            .replaceWith(translatedSolutionStatus.first());
//                }
//            }
//        }
//    }
//
//    private void translateArticleBody(Document document, Locale srcLocale,
//            Locale transLocale, BackendID backendID) throws BadRequestException,
//            ZanataMTException {
//        Elements sections = document.getElementsByTag("section");
//        for (Element section : sections) {
//            // section with id 'private-notes...' is non-translatable
//            if (KCSUtil.isPrivateNotes(section)) {
//                continue;
//            }
//            translateSection(section, srcLocale, transLocale, backendID);
//        }
//    }
//
//    private Element translateSection(Element section, Locale srcLocale,
//            Locale transLocale, BackendID backendID) throws BadRequestException,
//            ZanataMTException {
//        Map<String, Element> ignoreTranslationMap = Maps.newHashMap();
//
//        /**
//         * replace pre element with non-translatable node as placeholder
//         */
//        Elements codeElements = KCSUtil.getRawCodePreElements(section);
//        if (!codeElements.isEmpty()) {
//            for (Element element : codeElements) {
//                String id = String.valueOf(codeElements.indexOf(element));
//                ignoreTranslationMap.put(id, element.clone());
//                element.replaceWith(KCSUtil.generateNonTranslatableNode(id));
//            }
//        }
//        List<String> strings =
//                section.children().stream().map(Element::outerHtml)
//                        .collect(Collectors.toList());
//
//        // send section to translate
//        List<String> translations = persistentTranslationService.translate(strings,
//                srcLocale, transLocale, backendID, MediaType.TEXT_HTML_TYPE);
//        if (!translations.isEmpty()) {
//            int index = 0;
//            for (Element sectionChild : section.children()) {
//                sectionChild.replaceWith(
//                    DomUtil.parseAsElement(translations.get(index))
//                                .first());
//                index++;
//            }
//        }
//        // replace placeholder with initial element
//        for (Map.Entry<String, Element> entry : ignoreTranslationMap
//                .entrySet()) {
//            String id = KCSUtil.generateNodeName(entry.getKey());
//            Element placeholderElement = section.getElementById(id);
//            assert placeholderElement != null;
//
//            placeholderElement.replaceWith(entry.getValue());
//        }
//        return section;
//    }
}
