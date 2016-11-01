package org.zanata.mt.api;

import java.util.HashMap;
import java.util.Map;
import javax.ejb.TransactionAttribute;
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
            // use english strings if no translations
            translatedTitle =
                    StringUtils.isBlank(translatedTitle) ? article.getTitle()
                            : translatedTitle;

            String translatedBody =
                    translateBody(article.getDivContent(), srcLocale,
                            transLocale, provider, doc);

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
            Locale transLocale, Provider provider, org.zanata.mt.model.Document doc)
            throws TranslationEngineException, BadTranslationRequestException {
        Document document = Jsoup.parse(body);
        document = translateArticleHeader(document, srcLocale,
                transLocale, provider);
        document = translateArticleBody(document, srcLocale,
            transLocale, provider);
        return document.html();
    }

    private Document translateArticleHeader(Document document, Locale srcLocale,
            Locale transLocale, Provider provider)
            throws TranslationEngineException, BadTranslationRequestException {
        Elements header = document.getElementsByTag("header");
        Elements content = header.select("h1.title");
        Elements secondaryContent = header.select("span.status");

        String translatedContent =
                translationService.translate(content.html(), srcLocale,
                        transLocale, provider);

        if (StringUtils.isNotBlank(translatedContent)) {
            content.html(translatedContent);
        }

        String translatedSecondaryContent =
                translationService.translate(secondaryContent.html(),
                        srcLocale, transLocale, provider);
        if (StringUtils.isNotBlank(translatedSecondaryContent)) {
            secondaryContent.html(translatedSecondaryContent);
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
        for (Element sectionChild : section.children()) {
            if (TranslationUtil.isRawCodeParagraph(sectionChild)) {
                Map<Integer, Element> preMap = Maps.newHashMap();
                for (int i = 0; i < sectionChild.children().size(); i++) {
                    Element child = sectionChild.child(i);
                    // remove pre element from dom before translate
                    if (TranslationUtil.isPreElement(child)) {
                        preMap.put(i, child.clone());
                        child.remove();
                    }
                }
                // send sectionChild to translate
                String childHtmls =
                        translationService.translate(sectionChild.html(),
                                srcLocale, transLocale, provider);
                if (StringUtils.isNotBlank(childHtmls)) {
                    sectionChild.html(childHtmls);
                }

                // insert pre back into sectionChild
                for (Map.Entry<Integer, Element> entry : preMap.entrySet()) {
                    sectionChild.insertChildren(entry.getKey(),
                            Lists.newArrayList(entry.getValue()));
                }
            } else {
                String childHtmls =
                    translationService.translate(sectionChild.html(),
                        srcLocale, transLocale, provider);
                if (StringUtils.isNotBlank(childHtmls)) {
                    sectionChild.html(childHtmls);
                }
            }
        }
        return section;
    }
}