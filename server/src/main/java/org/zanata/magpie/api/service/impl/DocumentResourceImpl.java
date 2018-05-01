package org.zanata.magpie.api.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.DefaultProvider;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.DocumentStatistics;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.TypeString;
import org.zanata.magpie.api.service.DocumentResource;
import org.zanata.magpie.dao.LocaleDAO;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.process.DocumentProcessManager;
import org.zanata.magpie.service.DateRange;
import org.zanata.magpie.service.DocumentContentTranslatorService;
import org.zanata.magpie.service.DocumentService;
import org.zanata.magpie.util.UrlUtil;

import static org.zanata.magpie.model.BackendID.fromStringWithDefault;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class DocumentResourceImpl implements DocumentResource {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentResourceImpl.class);

    private DocumentContentTranslatorService documentContentTranslatorService;

    private LocaleDAO localeDAO;

    private DocumentService documentService;
    private boolean isDevMode;
    private BackendID defaultProvider;
    private Set<BackendID> availableProviders;

    @SuppressWarnings("unused")
    public DocumentResourceImpl() {
    }

    @Inject
    public DocumentResourceImpl(
            DocumentContentTranslatorService documentContentTranslatorService,
            LocaleDAO localeDAO, DocumentService documentService,
            @DevMode boolean isDevMode,
            @DefaultProvider BackendID defaultProvider,
            @BackEndProviders Set<BackendID> availableProviders) {
        this.documentContentTranslatorService =
                documentContentTranslatorService;
        this.localeDAO = localeDAO;
        this.documentService = documentService;
        this.isDevMode = isDevMode;
        this.defaultProvider = defaultProvider;
        this.availableProviders = availableProviders;
    }

    @Override
    public Response getStatistics(@QueryParam("url") String url,
            @QueryParam("fromLocaleCode") LocaleCode fromLocaleCode,
            @QueryParam("toLocaleCode") LocaleCode toLocaleCode,
            @QueryParam("dateRange") String dateRangeParam) {
        if (StringUtils.isBlank(url)) {
            APIResponse response =
                    new APIResponse(Response.Status.BAD_REQUEST, "Empty url");
            return Response.status(response.getStatus()).entity(response)
                    .build();
        }

        Optional<DateRange> dateParam =
                StringUtils.isBlank(dateRangeParam) ? Optional.empty() :
                        Optional.of(DateRange.from(dateRangeParam));

        List<Document> documents = documentService
                .getByUrl(url, Optional.ofNullable(fromLocaleCode),
                        Optional.ofNullable(toLocaleCode), dateParam);

        DocumentStatistics statistics = new DocumentStatistics(url);
        for (Document document: documents) {
            int wordCount = getTotalWordCount(document,
                    document.getFromLocale().getLocaleCode());

            statistics.addRequestCount(
                    document.getFromLocale().getLocaleCode().getId(),
                    document.getToLocale().getLocaleCode().getId(),
                    document.getCount(), wordCount);
        }
        return Response.ok().entity(statistics).build();
    }

    private int getTotalWordCount(Document doc, LocaleCode localeCode) {
        return doc.getTextFlows().values().stream()
                .filter(tf -> tf.getLocale().getLocaleCode()
                        .equals(localeCode))
                .mapToInt(tf -> tf.getWordCount().intValue())
                .sum();
    }

    @Override
    public Response translate(DocumentContent docContent,
            @QueryParam("toLocaleCode") LocaleCode toLocaleCode) {

        Optional<APIResponse> errorResp =
                validateTranslateRequest(docContent, toLocaleCode);
        if (errorResp.isPresent()) {
            return Response.status(errorResp.get().getStatus())
                    .entity(errorResp.get()).build();
        }

        // use dev backend if it's DEV mode
        final BackendID backendID = isDevMode ? BackendID.DEV
                : fromStringWithDefault(docContent.getBackendId(),
                        defaultProvider);

        // check if this backend is available
        if (!availableProviders.contains(backendID)) {
            LOG.warn("requested machine translation provider {} is not set up (no credential)", backendID);
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                    .entity(new APIResponse(Response.Status.NOT_IMPLEMENTED,
                            "Error: backendId " + docContent.getBackendId() + " not available")).build();
        }

        // if source locale == target locale, return docContent
        LocaleCode fromLocaleCode = new LocaleCode(docContent.getLocaleCode());
        if (fromLocaleCode.equals(toLocaleCode)) {
            LOG.info("Returning request as FROM and TO localeCode are the same: {}", fromLocaleCode);
            return Response.ok().entity(docContent).build();
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Request translations: {} toLocaleCode {} backendId: {}",
                    docContent, toLocaleCode, backendID.getId());
        }

        Locale fromLocale = getLocale(fromLocaleCode);
        Locale toLocale = getLocale(toLocaleCode);

        Document doc = documentService
                .getOrCreateByUrl(docContent.getUrl(), fromLocale,
                        toLocale);
        documentService.incrementDocRequestCount(doc);

        DocumentContent newDocContent = documentContentTranslatorService
                .translateDocument(doc, docContent, backendID);
        return Response.ok().entity(newDocContent).build();
    }

    private Optional<APIResponse> validateTranslateRequest(DocumentContent docContent,
            LocaleCode toLocaleCode) {
        if (toLocaleCode == null) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Invalid query param: toLocaleCode"));
        }
        if (docContent == null || docContent.getContents() == null ||
                docContent.getContents().isEmpty()) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Empty content:" + docContent));
        }
        if (StringUtils.isBlank(docContent.getLocaleCode())) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Empty localeCode"));
        }
        if (StringUtils.isBlank(docContent.getUrl()) ||
                !UrlUtil.isValidURL(docContent.getUrl())) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                    "Invalid url:" + docContent.getUrl()));
        }
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
        }
        return Optional.empty();
    }

    private Locale getLocale(LocaleCode localeCode) throws BadRequestException {
        if (localeCode == null) {
            return null;
        }
        Locale locale = localeDAO.getByLocaleCode(localeCode);
        if (locale == null) {
            throw new BadRequestException("Not supported locale:" + localeCode);
        }
        return locale;
    }
}
