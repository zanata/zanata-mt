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

@Path("/translate")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ArticleTranslatorResource {
    private static final Logger LOG =
        LoggerFactory.getLogger(ArticleTranslatorResource.class);

    private ArticleTranslatorService kcsArticleService;

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
    private LocaleId getLocaleFromMap(LocaleId locale) {
        return LOCALE_MAP.containsKey(locale) ? LOCALE_MAP.get(locale) : locale;
    }

    @SuppressWarnings("unused")
    public ArticleTranslatorResource() {
    }

    @Inject
    public ArticleTranslatorResource(ArticleTranslatorService kcsArticleService,
            LocaleDAO localeDAO, DocumentDAO documentDAO) {
        this.kcsArticleService = kcsArticleService;
        this.localeDAO = localeDAO;
        this.documentDAO = documentDAO;
    }

    @POST
    public Response translate(@NotNull Article article,
            @NotNull @QueryParam("sourceLang") LocaleId sourceLang,
            @NotNull @QueryParam("targetLang") LocaleId targetLang,
            @NotNull @QueryParam("backendId") String backendId) {

        Optional<APIErrorResponse> errorResp = validatePostRequest(article,
                sourceLang, targetLang, backendId);
        if (errorResp.isPresent()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResp.get()).build();
        }

        BackendID backendID = new BackendID(backendId);
        ArticleType articleType = new ArticleType(article.getArticleType());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request translations:" + article + " source_lang:"
                + sourceLang + " target_lang" + targetLang + " backendId:"
                + backendId);
        }

        Locale srcLocale = getLocale(sourceLang);
        Locale transLocale = getLocale(targetLang);

        org.zanata.mt.model.Document doc = documentDAO
            .getOrCreateByUrl(article.getUrl(), srcLocale, transLocale);

        try {
            ArticleTranslatorService articleTranslatorService = getResourceService(articleType);
            Article newArticle = articleTranslatorService
                .translateArticle(article, srcLocale, transLocale, backendID);
            doc.increaseUsedCount();
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
                new APIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e, title);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response)
                .build();
        }
    }

    private Optional<APIErrorResponse> validatePostRequest(Article article,
            LocaleId sourceLang, LocaleId targetLang, String backendId) {
        if (sourceLang == null || targetLang == null
                || StringUtils.isBlank(backendId)) {
            return Optional.of(new APIErrorResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: sourceLang, targetLang or provider"));
        }

        if (!backendId.equals(BackendID.MS)) {
            throw new ZanataMTException("Not supported backendId:" + backendId);
        }

        if (article == null || StringUtils.isBlank(article.getTitleText())
            && StringUtils.isBlank(article.getContentHTML())) {
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

    private ArticleTranslatorService getResourceService(ArticleType articleType)
        throws ZanataMTException {
        if (articleType.equals(ArticleType.KCS_ARTICLE)) {
            return kcsArticleService;
        }
        throw new ZanataMTException("Not supported articleType" + articleType);
    }
}
