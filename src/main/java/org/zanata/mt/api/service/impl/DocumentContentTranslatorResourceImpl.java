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
import org.zanata.mt.model.Locale;
import org.zanata.mt.service.DocumentContentTranslatorService;
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
            @QueryParam("targetLang") LocaleId targetLocaleId) {
        // Default to MS engine for translation
        BackendID backendID = BackendID.MS;

        Optional<APIResponse> errorResp =
                validateTranslateRequest(docContent, targetLocaleId);
        if (errorResp.isPresent()) {
            return Response.status(errorResp.get().getStatus())
                    .entity(errorResp.get()).build();
        }

        // if source locale == target locale, return docContent
        LocaleId srcLocaleId = new LocaleId(docContent.getLocale());
        if (srcLocaleId.equals(targetLocaleId)) {
            LOG.info("Returning request as source and target locale are the same:" + srcLocaleId);
            return Response.ok().entity(docContent).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request translations:" + docContent + " targetLang"
                    + targetLocaleId + " backendId:" + backendID.getId());
        }

        try {
            Locale srcLocale = getLocale(srcLocaleId);
            Locale transLocale = getLocale(targetLocaleId);

            org.zanata.mt.model.Document doc = documentDAO
                    .getOrCreateByUrl(docContent.getUrl(), srcLocale, transLocale);

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

    private Locale getLocale(@NotNull LocaleId localeId) {
        Locale locale = localeDAO.getByLocaleId(localeId);
        if (locale == null) {
            throw new BadRequestException("Not supported locale:" + localeId);
        }
        return locale;
    }
}
