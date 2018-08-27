package org.zanata.magpie.api.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fedorahosted.tennera.jgettext.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.DefaultProvider;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.DocumentStatistics;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.TranslateDocumentForm;
import org.zanata.magpie.api.dto.TypeString;
import org.zanata.magpie.api.service.BackendResource;
import org.zanata.magpie.api.service.DocumentResource;
import org.zanata.magpie.dao.LocaleDAO;
import org.zanata.magpie.filter.TranslationFileAdapter;
import org.zanata.magpie.filter.PoFileAdapter;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.service.DocumentContentTranslatorService;
import org.zanata.magpie.service.DocumentService;
import org.zanata.magpie.util.UrlUtil;

import static org.zanata.magpie.api.service.BackendResource.ATTRIBUTION_KEY;
import static org.zanata.magpie.model.BackendID.fromStringWithDefault;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class DocumentResourceImpl implements DocumentResource {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentResourceImpl.class);

    @Context
    protected UriInfo uriInfo;

    private DocumentContentTranslatorService documentContentTranslatorService;
    private LocaleDAO localeDAO;
    private BackendResource backendResourceImpl;
    private DocumentService documentService;
    private boolean isDevMode;
    private BackendID defaultProvider;
    private Set<BackendID> availableProviders;

    private static ImmutableMap<String, TranslationFileAdapter> FILTERS =
        ImmutableMap.<String, TranslationFileAdapter>builder()
            .put("POT", new PoFileAdapter(StandardCharsets.UTF_8))
            .build();

    @SuppressWarnings("unused")
    public DocumentResourceImpl() {
    }

    @Inject
    public DocumentResourceImpl(
        DocumentContentTranslatorService documentContentTranslatorService,
        LocaleDAO localeDAO,
        DocumentService documentService,
        BackendResource backendResourceImpl,
        @DevMode boolean isDevMode,
        @DefaultProvider BackendID defaultProvider,
        @BackEndProviders Set<BackendID> availableProviders) {
        this.documentContentTranslatorService =
                documentContentTranslatorService;
        this.localeDAO = localeDAO;
        this.documentService = documentService;
        this.backendResourceImpl = backendResourceImpl;
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

        // TODO this could be faster if we eagerly fetch the TextFlows for all Documents
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

    // lazily loads the text flows in doc
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
            LOG.warn(
                "requested machine translation provider {} is not set up (no credential)",
                backendID);
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(new APIResponse(Response.Status.NOT_IMPLEMENTED,
                    "Error: backendId " + docContent.getBackendId() +
                        " not available")).build();
        }

        // if source locale == target locale, return docContent
        LocaleCode fromLocaleCode = new LocaleCode(docContent.getLocaleCode());
        if (fromLocaleCode.equals(toLocaleCode)) {
            LOG.info(
                "Returning unchanged request, FROM and TO localeCode are the same {}",
                fromLocaleCode);
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

    @Override
    public Response translateFile(TranslateDocumentForm form,
        LocaleCode fromLocaleCode, LocaleCode toLocaleCode) {

        if (fromLocaleCode == null || toLocaleCode == null) {
            APIResponse apiResponse =
                new APIResponse(Response.Status.BAD_REQUEST,
                    "Null query param: fromLocaleCode and toLocaleCode");
            return Response.status(apiResponse.getStatus())
                .entity(apiResponse).build();
        }

        if (StringUtils.isBlank(form.getFileName())) {
            APIResponse apiResponse =
                new APIResponse(Response.Status.BAD_REQUEST,
                    "Null form param: fileName");
            return Response.status(apiResponse.getStatus())
                .entity(apiResponse).build();
        }

        try {
            String fileExt = FilenameUtils.getExtension(form.getFileName());
            TranslationFileAdapter adapter = FILTERS.get(fileExt.toUpperCase());

            String url = getURL(uriInfo.getRequestUri().toString(), form.getFileName());
            Pair<DocumentContent, Map<String, Message>> contents = adapter
                .parseSourceDocument(form.getFileStream(), url, fromLocaleCode);

            Response response = this.translate(contents.getLeft(), toLocaleCode);
            if (response.getStatus() != Response.Status.OK.getStatusCode()) {
                return response;
            }
            DocumentContent newDocContent =
                (DocumentContent) response.getEntity();
            Response attr = backendResourceImpl
                .getStringAttribution(newDocContent.getBackendId());
            String attribution = attr != null &&
                attr.getStatus() == Response.Status.OK.getStatusCode() ?
                ATTRIBUTION_KEY + ":" + attr.getEntity().toString() : "";

            FilterStreamingOutput stream =
                new FilterStreamingOutput(adapter, newDocContent,
                    contents.getRight(), fromLocaleCode,
                    toLocaleCode, attribution);

            String docName =
                getTransFilename(adapter.getTranslationFileExtension(),
                    form.getFileName(), toLocaleCode.getId());

            return Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM)
                .header("content-disposition",
                    "attachment; filename=\"" + docName + "\"")
                .header("Access-Control-Expose-Headers",
                    "content-disposition")
                .build();
        } catch (Exception e) {
            APIResponse apiResponse =
                new APIResponse(Response.Status.BAD_REQUEST,
                    e, "Unable to parse document");
            LOG.error(apiResponse.getDetails(), e);
            return Response.status(apiResponse.getStatus())
                .entity(apiResponse).build();
        }
    }

    private String getURL(String requestURL, String fileName) {
        String url = requestURL.endsWith("/") ? requestURL.substring(0, requestURL.length() - 1) : requestURL;
        String name = "&name=" + (fileName.startsWith("/") ?  fileName.substring(1) : fileName);
        return url + name;
    }

    private String getTransFilename(String ext, String filename,
        String localeCode) {
        String transFilename =
            FilenameUtils.removeExtension(filename) + "_" +
                localeCode + "." + ext;
        return transFilename;
    }

    private Optional<APIResponse> validateTranslateRequest(
        DocumentContent docContent,
        LocaleCode toLocaleCode) {
        if (toLocaleCode == null) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                "Invalid query param: toLocaleCode"));
        }
        if (docContent == null || docContent.getContents() == null ||
            docContent.getContents().isEmpty()) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                "Empty content: " + docContent));
        }
        if (StringUtils.isBlank(docContent.getLocaleCode())) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                "Blank localeCode"));
        }
        if (StringUtils.isBlank(docContent.getUrl()) ||
            !UrlUtil.isValidURL(docContent.getUrl())) {
            return Optional.of(new APIResponse(Response.Status.BAD_REQUEST,
                "Invalid url: " + docContent.getUrl()));
        }
        for (TypeString string : docContent.getContents()) {
            if (!documentContentTranslatorService
                .isMediaTypeSupported(string.getType())) {
                return Optional
                    .of(new APIResponse(Response.Status.BAD_REQUEST,
                        "Unsupported media type: " + string.getType()));
            }
        }
        return Optional.empty();
    }

    private @Nullable
    Locale getLocale(LocaleCode localeCode) throws BadRequestException {
        if (localeCode == null) {
            return null;
        }
        Locale locale = localeDAO.getByLocaleCode(localeCode);
        if (locale != null) {
            return locale;
        }
        LocaleCode langCode = new LocaleCode(localeCode.getLanguage());
        Locale langLocale = localeDAO.getByLocaleCode(langCode);
        if (langLocale != null) {
            return langLocale;
        }
        throw new BadRequestException(
            "Unsupported locale: " + localeCode);
    }

    protected static class FilterStreamingOutput implements StreamingOutput {
        protected final TranslationFileAdapter filter;
        protected final DocumentContent translatedDocContent;
        protected final LocaleCode fromLocaleCode;
        protected final LocaleCode toLocaleCode;
        protected final String attribution;
        protected final Map<String, Message> messages;

        FilterStreamingOutput(TranslationFileAdapter filter,
            DocumentContent translatedDocContent, Map<String, Message> messages,
            LocaleCode fromLocaleCode, LocaleCode toLocaleCode,
            String attribution) {
            this.filter = filter;
            this.translatedDocContent = translatedDocContent;
            this.messages = messages;
            this.fromLocaleCode = fromLocaleCode;
            this.toLocaleCode = toLocaleCode;
            this.attribution = attribution;
        }

        @Override
        public void write(OutputStream output)
            throws IOException, WebApplicationException {
            filter.writeTranslatedFile(output, fromLocaleCode,
                toLocaleCode, translatedDocContent, messages, attribution);
        }
    }
}
