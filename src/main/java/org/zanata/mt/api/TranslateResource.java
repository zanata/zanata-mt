package org.zanata.mt.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.APIErrorResponse;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.exception.BadTranslationRequestException;
import org.zanata.mt.exception.TranslationEngineException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.util.TranslationUtil;
import org.zanata.mt.service.TranslationService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.zanata.mt.util.UrlUtil;

@Path("/translate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class TranslateResource {
    private static final Logger log =
        LoggerFactory.getLogger(TranslateResource.class);

    @Inject
    private TranslationService translationService;

    @Inject
    private LocaleDAO localeDAO;

    @Inject
    private DocumentDAO documentDAO;

    /**
     * Default Machine translation provider: {@link Provider#MS}
     */
    private static final String DEFAULT_PROVIDER = "MS";

    /**
     * Locale mapping
     */
    private static final HashMap<LocaleId, LocaleId> LocaleMap = Maps.newHashMap();
    static
    {
        LocaleMap.put(new LocaleId("zh-hans"), LocaleId.ZH_CN);
    }
    private LocaleId getLocaleFromMap(LocaleId locale) {
        return LocaleMap.containsKey(locale) ? LocaleMap.get(locale) : locale;
    }

    @POST
    @TransactionAttribute
    public Response translate(Article article,
            @NotNull @QueryParam("sourceLang") LocaleId sourceLang,
            @NotNull @QueryParam("targetLang") LocaleId targetLang,
            @QueryParam("provider") @DefaultValue(DEFAULT_PROVIDER) String providerString) {
        log.debug("Request translations:" + article.toString() + " source_lang:"
                + sourceLang + " target_lang" + targetLang + " provider:"
                + providerString);
        if (sourceLang == null || targetLang == null) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: sourceLang, targetLang");
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                .build();
        }

        Provider provider = null;
        try {
            provider = Provider.valueOf(providerString);
        } catch (IllegalArgumentException e) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST, e,
                    "Invalid provider:" + providerString);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(response).build();
        }

        if (StringUtils.isBlank(article.getUrl()) ||
            !UrlUtil.isValidURL(article.getUrl())) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + article.getUrl());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(response).build();
        }

        if (StringUtils.isBlank(article.getTitle())
                && StringUtils.isBlank(article.getDivContent())) {
            return Response.ok().entity(article).build();
        }

        LocaleId searchLocale = getLocaleFromMap(sourceLang);
        Locale srcLocale = localeDAO.getOrCreateByLocaleId(searchLocale);

        searchLocale = getLocaleFromMap(targetLang);
        Locale transLocale = localeDAO.getOrCreateByLocaleId(searchLocale);

        org.zanata.mt.model.Document doc = documentDAO
            .getOrCreateByUrl(article.getUrl(), srcLocale, transLocale);

        try {
            String translatedTitle =
                    translationService.translate(article.getTitle(), srcLocale,
                            transLocale, provider);

            String translatedBody =
                    translateBody(article.getDivContent(), srcLocale,
                            transLocale, provider);

            Article newArticle =
                new Article(translatedTitle, translatedBody, article.getUrl());

            doc.incrementCount();
            documentDAO.persist(doc);
            return Response.ok().entity(newArticle).build();
        } catch (BadTranslationRequestException e) {
            String title = "Bad request";
            log.error(title , e);
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                .build();
        } catch (TranslationEngineException e) {
            String title = "Unable to get translations from engine";
            log.error(title, e);
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.serverError().entity(response).build();
        }
    }

    private String translateBody(String body, Locale srcLocale,
            Locale transLocale, Provider provider)
            throws TranslationEngineException, BadTranslationRequestException {
        Document document = Jsoup.parse(body);
        document = translateArticleHeader(document, srcLocale, transLocale,
                provider);
        document = translateArticleBody(document, srcLocale, transLocale,
                provider);
        return document.html();
    }

    private Document translateArticleHeader(Document document, Locale srcLocale,
            Locale transLocale, Provider provider)
            throws TranslationEngineException, BadTranslationRequestException {
        Elements header = document.getElementsByTag("header");
        Elements content = header.select("h1.title");
        Elements secondaryContent = header.select("span.status");
        
        List<String> headers =
                Lists.newArrayList(content.html(), secondaryContent.html());
        List<String> translations = translationService.translate(headers,
                srcLocale, transLocale, provider);

        if (!translations.isEmpty()) {
            content.html(translations.get(0));
            secondaryContent.html(translations.get(1));
        }
        return document;
    }

    private Document translateArticleBody(Document document, Locale srcLocale,
            Locale transLocale, Provider provider)
            throws TranslationEngineException, BadTranslationRequestException {
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
            throws TranslationEngineException, BadTranslationRequestException {
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
        List<String> strings = section.children().stream().map(Element::html)
            .collect(Collectors.toList());

        // send section to translate
        List<String> translations = translationService.translate(strings,
                srcLocale, transLocale, provider);
        if (!translations.isEmpty()) {
            int index = 0;
            for (Element sectionChild: section.children()) {
                sectionChild.html(translations.get(index));
                index ++;
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