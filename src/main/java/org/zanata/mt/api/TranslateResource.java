package org.zanata.mt.api;

import java.util.HashMap;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.APIErrorResponse;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ContentType;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.Provider;
import org.zanata.mt.service.ResourceService;

import com.google.common.collect.Maps;
import org.zanata.mt.util.UrlUtil;

@Path("/translate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class TranslateResource {
    private static final Logger LOG =
        LoggerFactory.getLogger(TranslateResource.class);

    private ResourceService kcsArticleService;

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
    private static final HashMap<LocaleId, LocaleId> LOCALE_MAP = Maps.newHashMap();
    static {
        LOCALE_MAP.put(new LocaleId("zh-hans"), LocaleId.ZH_CN);
    }
    private LocaleId getLocaleFromMap(LocaleId locale) {
        return LOCALE_MAP.containsKey(locale) ? LOCALE_MAP.get(locale) : locale;
    }

    @SuppressWarnings("unused")
    public TranslateResource() {
    }

    @Inject
    public TranslateResource(ResourceService kcsArticleService,
            LocaleDAO localeDAO, DocumentDAO documentDAO) {
        this.kcsArticleService = kcsArticleService;
        this.localeDAO = localeDAO;
        this.documentDAO = documentDAO;
    }

    @POST
    public Response translate(@NotNull Article article,
            @NotNull @QueryParam("sourceLang") LocaleId sourceLang,
            @NotNull @QueryParam("targetLang") LocaleId targetLang,
            @QueryParam("provider") @DefaultValue(DEFAULT_PROVIDER) String providerStr,
            @QueryParam("contentType") @DefaultValue(DEFAULT_CONTENT_TYPE) String contentTypeStr) {

        Optional<APIErrorResponse> errorResp = validatePostRequest(article,
                sourceLang, targetLang, providerStr, contentTypeStr);
        if (errorResp.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResp.get()).build();
        }

        Provider provider = Provider.valueOf(providerStr);
        ContentType contentType = ContentType.valueOf(contentTypeStr);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request translations:" + article + " source_lang:"
                + sourceLang + " target_lang" + targetLang + " provider:"
                + providerStr);
        }

        Locale srcLocale = getLocale(sourceLang);
        Locale transLocale = getLocale(targetLang);

        org.zanata.mt.model.Document doc = documentDAO
            .getOrCreateByUrl(article.getUrl(), srcLocale, transLocale);

        try {
            ResourceService resourceService = getResourceService(contentType);
            Article newArticle = resourceService
                .translateArticle(article, srcLocale, transLocale, provider);
            doc.incrementUsedCount();
            documentDAO.persist(doc);
            return Response.ok().entity(newArticle).build();
        } catch (BadRequestException | ZanataMTException e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                    .build();
        }
    }

    private Optional<APIErrorResponse> validatePostRequest(Article article,
            LocaleId sourceLang, LocaleId targetLang, String providerStr,
            String contentTypeStr) {
        if (sourceLang == null || targetLang == null
                || StringUtils.isBlank(providerStr)) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: sourceLang, targetLang or provider"));
        }

        try {
            Provider.valueOf(providerStr);
        } catch (IllegalArgumentException e) {
            return Optional
                    .of(new APIErrorResponse(Response.Status.BAD_REQUEST, e,
                            "Provider not supported:" + providerStr));
        }

        try {
            ContentType.valueOf(contentTypeStr);
        } catch (IllegalArgumentException e) {
            Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST, e,
                    "ContentType not supported:" + contentTypeStr));
        }

        if (article == null || StringUtils.isBlank(article.getTitle())
            && StringUtils.isBlank(article.getContent())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                "Empty content:" + article));
        }
        if (StringUtils.isBlank(article.getUrl()) ||
            !UrlUtil.isValidURL(article.getUrl())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + article.getUrl()));
        }
        return Optional.empty();
    }

    private Locale getLocale(@NotNull LocaleId localeId) {
        LocaleId searchLocale = getLocaleFromMap(localeId);
        return localeDAO.getOrCreateByLocaleId(searchLocale);
    }

    private ResourceService getResourceService(ContentType contentType)
        throws ZanataMTException {
        if (contentType.equals(ContentType.KCS_ARTICLE)) {
            return kcsArticleService;
        }
        throw new ZanataMTException("Not supported contentType" + contentType);
    }
}
