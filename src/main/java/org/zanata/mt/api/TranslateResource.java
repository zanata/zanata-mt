package org.zanata.mt.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
import org.zanata.mt.exception.TranslationProviderException;
import org.zanata.mt.model.ContentType;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.service.impl.KCSResourceService;
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

    private KCSResourceService kcsResourceService;

    private LocaleDAO localeDAO;

    private DocumentDAO documentDAO;

    /**
     * Default Machine translation provider: {@link Provider#MS}
     */
    private static final String DEFAULT_PROVIDER = "MS";

    /**
     * Default content type: {@link org.zanata.mt.model.ContentType#KCS_ARTICLE}
     */
    private static final String DEFAULT_CONTENT_TYPE = "KCS_ARTICLE";

    /**
     * Locale mapping
     *
     * TODO: make this configurable, at the moment it is done manually.
     */
    private static final HashMap<LocaleId, LocaleId> LocaleMap = Maps.newHashMap();
    static
    {
        LocaleMap.put(new LocaleId("zh-hans"), LocaleId.ZH_CN);
    }
    private LocaleId getLocaleFromMap(LocaleId locale) {
        return LocaleMap.containsKey(locale) ? LocaleMap.get(locale) : locale;
    }

    @SuppressWarnings("unused")
    public TranslateResource() {
    }

    @Inject
    public TranslateResource(TranslationService translationService,
            LocaleDAO localeDAO, DocumentDAO documentDAO) {
        this.translationService = translationService;
        this.localeDAO = localeDAO;
        this.documentDAO = documentDAO;
    }

    @POST
    public Response translate(@NotNull Article article,
            @NotNull @QueryParam("sourceLang") LocaleId sourceLang,
            @NotNull @QueryParam("targetLang") LocaleId targetLang,
            @QueryParam("provider") @DefaultValue(DEFAULT_PROVIDER) String providerStr,
            @QueryParam("contentType") @DefaultValue(DEFAULT_CONTENT_TYPE) String contentTypeStr) {
        if (sourceLang == null || targetLang == null
                || StringUtils.isBlank(providerStr)) {
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.BAD_REQUEST,
                            "Invalid query param: sourceLang, targetLang or provider");
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                    .build();
        }

        Provider provider = null;
        try {
            provider = Provider.valueOf(providerStr);
        } catch (IllegalArgumentException e) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST, e,
                    "Provider not supported:" + providerStr);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(response).build();
        }
        ContentType contentType = null;
        try {
            contentType = ContentType.valueOf(contentTypeStr);
        } catch (IllegalArgumentException e) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST, e,
                    "ContentType not supported:" + contentType);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(response).build();
        }

        if (article == null) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid entity:" + article);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                .build();
        }
        //return ok if there's no content in article
        if (StringUtils.isBlank(article.getTitle())
            && StringUtils.isBlank(article.getContent())) {
            return Response.ok().entity(article).build();
        }
        if (StringUtils.isBlank(article.getUrl()) ||
            !UrlUtil.isValidURL(article.getUrl())) {
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + article.getUrl());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(response).build();
        }

        log.debug("Request translations:" + article + " source_lang:"
            + sourceLang + " target_lang" + targetLang + " provider:"
            + providerStr);

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

            String translatedContent =
                    translateContent(article.getContent(), srcLocale,
                            transLocale, provider);

            Article newArticle =
                new Article(translatedTitle, translatedContent, article.getUrl());

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
        } catch (TranslationProviderException e) {
            String title = "Unable to get translations from engine";
            log.error(title, e);
            APIErrorResponse response =
                new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.serverError().entity(response).build();
        }
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
            throws TranslationProviderException, BadTranslationRequestException {
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