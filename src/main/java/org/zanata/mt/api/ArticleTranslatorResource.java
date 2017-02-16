package org.zanata.mt.api;

import java.util.HashMap;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
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
import org.zanata.mt.api.dto.RawArticle;
import org.zanata.mt.api.dto.Article;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.ArticleType;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.service.ArticleTranslatorService;

import com.google.common.collect.Maps;
import org.zanata.mt.util.UrlUtil;

/**
 * API entry point for article translation: '/translate'
 *
 * See
 * {@link #translate(Article, LocaleId)}
 * {@link #translate(RawArticle, LocaleId)}
 *
 */
@Path("/translate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ArticleTranslatorResource {
    private static final Logger LOG =
        LoggerFactory.getLogger(ArticleTranslatorResource.class);

    private ArticleTranslatorService articleTranslatorService;

    private LocaleDAO localeDAO;

    private DocumentDAO documentDAO;

    /**
     * Locale mapping
     *
     * TODO: make this configurable, at the moment it is done manually.
     */
    private static final HashMap<LocaleId, LocaleId> LOCALE_MAP = Maps.newHashMap();
    static {
        LOCALE_MAP.put(new LocaleId("zh-hans"), LocaleId.ZH_CN);
    }

    @SuppressWarnings("unused")
    public ArticleTranslatorResource() {
    }

    @Inject
    public ArticleTranslatorResource(
            ArticleTranslatorService articleTranslatorService,
            LocaleDAO localeDAO, DocumentDAO documentDAO) {
        this.articleTranslatorService = articleTranslatorService;
        this.localeDAO = localeDAO;
        this.documentDAO = documentDAO;
    }

    @POST
    public Response translate(@NotNull Article article,
            @NotNull @QueryParam("targetLang") LocaleId targetLang) {
        // Default to MS engine for translation
        BackendID backendID = BackendID.MS;

        Optional<APIErrorResponse> errorResp =
                validatePostRequest(article, targetLang);
        if (errorResp.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResp.get()).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.info("Request translations:" + article + " target_lang"
                    + targetLang + " backendId:" + backendID.getId());
        }

        Locale srcLocale = getLocale(new LocaleId(article.getLocale()));
        Locale transLocale = getLocale(targetLang);

        org.zanata.mt.model.Document doc = documentDAO
                .getOrCreateByUrl(article.getUrl(), srcLocale, transLocale);

        try {
            Article newArticle = articleTranslatorService
                    .translateArticle(article, srcLocale, transLocale,
                            backendID);
            doc.incrementUsedCount();
            documentDAO.persist(doc);
            return Response.ok().entity(newArticle).build();
        } catch (BadRequestException e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                    .build();
        } catch (ZanataMTException e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                            e, title);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }

    @POST
    @Path("/raw")
    public Response translate(@NotNull RawArticle article,
            @NotNull @QueryParam("targetLang") LocaleId targetLang) {

        // Default to MS engine for translation
        BackendID backendID = BackendID.MS;

        Optional<APIErrorResponse> errorResp =
                validatePostRequest(article, targetLang);
        if (errorResp.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResp.get()).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.info("Request translations:" + article + " target_lang"
                    + targetLang + " backendId:" + backendID.getId());
        }

        Locale srcLocale = getLocale(new LocaleId(article.getLocale()));
        Locale transLocale = getLocale(targetLang);

        org.zanata.mt.model.Document doc = documentDAO
            .getOrCreateByUrl(article.getUrl(), srcLocale, transLocale);

        try {
            RawArticle newRawArticle = articleTranslatorService
                    .translateArticle(article, srcLocale, transLocale,
                            backendID);
            doc.incrementUsedCount();
            documentDAO.persist(doc);
            return Response.ok().entity(newRawArticle).build();
        } catch (BadRequestException e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                    .build();
        } catch (ZanataMTException e) {
            String title = "Error";
            LOG.error(title, e);
            APIErrorResponse response =
                    new APIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR,
                            e, title);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }

    private Optional<APIErrorResponse> validatePostRequest(RawArticle rawArticle,
            LocaleId targetLang) {
        if (targetLang == null) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: targetLang"));
        }
        if (rawArticle == null || StringUtils.isBlank(rawArticle.getTitleText())
            && StringUtils.isBlank(rawArticle.getContentHTML())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                "Empty content"));
        }
        if (StringUtils.isBlank(rawArticle.getLocale())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                "Empty locale"));
        }
        if (rawArticle.getArticleType() == null) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Empty articleType"));
        }
        if (!rawArticle.getArticleType()
                .equals(ArticleType.KCS_ARTICLE.getType())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Not supported articleType:" +
                            rawArticle.getArticleType()));
        }
        if (StringUtils.isBlank(rawArticle.getUrl()) ||
            !UrlUtil.isValidURL(rawArticle.getUrl())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + rawArticle.getUrl()));
        }
        return Optional.empty();
    }

    private Optional<APIErrorResponse> validatePostRequest(Article article,
            LocaleId targetLang) {
        if (targetLang == null) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: targetLang"));
        }
        if (article == null || article.getContents() == null ||
                article.getContents().isEmpty()) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Empty content:" + article));
        }
        if (StringUtils.isBlank(article.getLocale())) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Empty locale"));
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

    private LocaleId getLocaleFromMap(LocaleId locale) {
        return LOCALE_MAP.containsKey(locale) ? LOCALE_MAP.get(locale) : locale;
    }
}
