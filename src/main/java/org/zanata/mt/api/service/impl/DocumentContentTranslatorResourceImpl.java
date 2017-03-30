package org.zanata.mt.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.api.service.DocumentContentTranslatorResource;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.dao.LocaleDAO;
import org.zanata.mt.model.BackendID;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;
import org.zanata.mt.service.DocumentContentTranslatorService;
import org.zanata.mt.util.ExceptionUtil;
import org.zanata.mt.util.UrlUtil;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class DocumentContentTranslatorResourceImpl
        implements DocumentContentTranslatorResource {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentContentTranslatorResourceImpl.class);

    private DocumentContentTranslatorService documentContentTranslatorService;

    private LocaleDAO localeDAO;

    private DocumentDAO documentDAO;

    @SuppressWarnings("unused")
    public DocumentContentTranslatorResourceImpl() {
    }

    @Inject
    public DocumentContentTranslatorResourceImpl(
            DocumentContentTranslatorService documentContentTranslatorService,
            LocaleDAO localeDAO, DocumentDAO documentDAO) {
        this.documentContentTranslatorService =
                documentContentTranslatorService;
        this.localeDAO = localeDAO;
        this.documentDAO = documentDAO;
    }

    @Override
    public Response translate(DocumentContent docContent,
            @QueryParam("targetLang") LocaleId targetLang) {
        // Default to MS engine for translation
        BackendID backendID = BackendID.MS;

        Optional<APIResponse> errorResp =
                validateTranslateRequest(docContent, targetLang);
        if (errorResp.isPresent()) {
            return Response.status(errorResp.get().getStatus())
                    .entity(errorResp.get()).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request translations:" + docContent + " target_lang"
                    + targetLang + " backendId:" + backendID.getId());
        }

        Locale srcLocale = getLocale(new LocaleId(docContent.getLocale()));
        Locale transLocale = getLocale(targetLang);

        Document doc = fetchOrCreateDocument(docContent.getUrl(), srcLocale, transLocale);

        try {
            DocumentContent newDocContent = documentContentTranslatorService
                    .translateDocument(doc, docContent, srcLocale, transLocale,
                            backendID);
            doc.incrementUsedCount();
            documentDAO.persist(doc);
            return Response.ok().entity(newDocContent).build();
        } catch (BadRequestException e) {
            String title = "Error";
            LOG.error(title, e);
            APIResponse response =
                    new APIResponse(Response.Status.BAD_REQUEST, e, title);
            return Response.status(Response.Status.BAD_REQUEST).entity(response)
                    .build();
        } catch (Exception e) {
            String title = "Error";
            LOG.error(title, e);
            APIResponse response =
                    new APIResponse(Response.Status.INTERNAL_SERVER_ERROR,
                            e, title);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(response)
                    .build();
        }
    }

    private Optional<APIResponse> validateTranslateRequest(DocumentContent docContent,
            LocaleId targetLang) {
        if (targetLang == null) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: targetLang"));
        }
        if (docContent == null || docContent.getContents() == null ||
                docContent.getContents().isEmpty()) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Empty content:" + docContent));
        }
        if (StringUtils.isBlank(docContent.getLocale())) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Empty locale"));
        }
        if (StringUtils.isBlank(docContent.getUrl()) ||
                !UrlUtil.isValidURL(docContent.getUrl())) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + docContent.getUrl()));
        }
        int totalChar = 0;
        for (TypeString string : docContent.getContents()) {
            if (StringUtils.isBlank(string.getValue()) ||
                    StringUtils.isBlank(string.getType())) {
                return Optional
                        .of(new APIResponse(Response.Status.BAD_REQUEST,
                                "Empty content: " + string.toString()));
            }
            if (!documentContentTranslatorService
                    .isMediaTypeSupported(string.getType())) {
                return Optional
                        .of(new APIResponse(Response.Status.BAD_REQUEST,
                                "Invalid mediaType: " + string.getType()));
            }
            totalChar += string.getValue().length();
        }
        if (totalChar > MAX_LENGTH) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Requested string length is more than " + MAX_LENGTH));
        }
        if (totalChar > MAX_LENGTH_WARN) {
            LOG.warn("Requested string length is more than " + MAX_LENGTH_WARN);
        }
        return Optional.empty();
    }

    /**
     * This is to handle concurrent db request for 2 same Document is being
     * persisted at the same time.
     */
    private Document fetchOrCreateDocument(String url, Locale srcLocale,
            Locale transLocale) {
        Document doc =
                documentDAO.getByUrl(url, srcLocale, transLocale);
        try {
            if (doc == null) {
                doc = documentDAO
                        .persist(new Document(url, srcLocale, transLocale));
                documentDAO.flush();
            }
        } catch (Exception e) {
            if (ExceptionUtil.isConstraintViolationException(e)) {
                doc = documentDAO.getByUrl(url, srcLocale, transLocale);
            }
        } finally {
            return doc;
        }
    }


    private Locale getLocale(@NotNull LocaleId localeId) {
        Locale locale = localeDAO.getByLocaleId(localeId);
        if (locale == null) {
            throw new BadRequestException("Not supported locale:" + localeId);
        }
        return locale;
    }
}
