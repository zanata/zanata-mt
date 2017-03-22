package org.zanata.mt.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.util.ArticleUtil;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.Locale;
import org.zanata.mt.model.BackendID;

import com.google.common.collect.Lists;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class DocumentContentTranslatorService {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentContentTranslatorService.class);


    // threshold for maximum length allowed for placeholder element
    public static final int PLACEHOLDER_THRESHOLD = 100;

    private PersistentTranslationService persistentTranslationService;

    @SuppressWarnings("unused")
    public DocumentContentTranslatorService() {
    }

    @Inject
    public DocumentContentTranslatorService(
            PersistentTranslationService persistentTranslationService) {
        this.persistentTranslationService = persistentTranslationService;
    }

    private void processPlaceholderElement(TypeString typeString,
            List<String> htmls, LinkedHashMap<Integer, TypeString> indexHTMLMap,
            List<APIResponse> warnings, String msg, int index) {
        int totalLength = htmls.stream().mapToInt(html -> html.length()).sum();
        if (totalLength >= PLACEHOLDER_THRESHOLD) {
            LOG.warn(msg + " - " + htmls);
            warnings.add(new APIResponse(Response.Status.OK, msg + " - " + htmls));
        } else {
            indexHTMLMap.put(index, typeString);
        }
    }

    /**
     * Translate a Document and send in for machine translation request
     * in batch group by media type.
     *
     * Non-translatable element:
     * {@link ArticleUtil#containsNonTranslatableNode(String)}
     * if it is more than {@link #PLACEHOLDER_THRESHOLD},
     * string will be excluded from translation request, with warning in the
     * response.
     *
     * Element that contain private-notes:
     * {@link ArticleUtil#containsPrivateNotes(String)}
     * if it is more than {@link #PLACEHOLDER_THRESHOLD},
     * string will be excluded from translation request, with warning in the
     * response.
     *
     * Element that contain code-section:
     * {@link ArticleUtil#containsKCSCodeSection(String)}
     * if it is more than {@link #PLACEHOLDER_THRESHOLD},
     * string will be excluded from translation request, with warning in the
     * response.
     *
     * {@link DocumentContent}
     **/
    public DocumentContent translateDocument(DocumentContent documentContent,
            Locale srcLocale, Locale transLocale, BackendID backendID)
            throws BadRequestException, ZanataMTException {

        LinkedHashMap<Integer, TypeString> indexHTMLMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, TypeString> indexTextMap = new LinkedHashMap<>();

        List<APIResponse> warnings = Lists.newArrayList();

        int index = 0;
        for (TypeString typeString: documentContent.getContents()) {
            MediaType mediaType = getMediaType(typeString.getType());

            if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                indexTextMap.put(index, typeString);
            } else if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                String html = typeString.getValue();
                if (ArticleUtil.containsNonTranslatableNode(html)) {
                    String warning =
                            "Warning: translation skipped: elements with translate=no should be replaced with placeholders.";
                    processPlaceholderElement(typeString,
                            ArticleUtil.getNonTranslatableHtml(html),
                            indexHTMLMap, warnings, warning, index);
                } else if (ArticleUtil.containsPrivateNotes(html)) {
                    String warning =
                            "Warning: translation skipped: private-notes elements should be omitted or replaced with placeholders.";
                    processPlaceholderElement(typeString,
                            ArticleUtil.getPrivateNotesHtml(html),
                            indexHTMLMap, warnings, warning, index);
                } else if (ArticleUtil.containsKCSCodeSection(html)) {
                    String warning =
                            "Warning: translation skipped: code-raw elements should be replaced with placeholders.";
                    processPlaceholderElement(typeString,
                            ArticleUtil.getKCSCodeHtml(html),
                            indexHTMLMap, warnings, warning, index);
                } else {
                    indexHTMLMap.put(index, typeString);
                }
            }
            index++;
        }

        List<TypeString> results = Lists.newArrayList(documentContent.getContents());

        if (!indexHTMLMap.isEmpty()) {
            List<String> translatedHtmls =
                    translateStrings(indexHTMLMap, srcLocale, transLocale,
                            backendID, MediaType.TEXT_HTML_TYPE);

            transferToResults(translatedHtmls, indexHTMLMap, results,
                    MediaType.TEXT_HTML);
        }

        if (!indexTextMap.isEmpty()) {
            List<String> translatedStrings =
                    translateStrings(indexTextMap, srcLocale, transLocale,
                            backendID, MediaType.TEXT_PLAIN_TYPE);

            transferToResults(translatedStrings, indexTextMap, results,
                    MediaType.TEXT_PLAIN);
        }

        return new DocumentContent(results, documentContent.getUrl(),
                transLocale.getLocaleId().getId(), backendID.getId(), warnings);
    }

    // translate all string values in map with given mediaType
    private List<String> translateStrings(
            LinkedHashMap<Integer, TypeString> indexMap, Locale srcLocale,
            Locale transLocale, BackendID backendID, MediaType mediaType)
            throws BadRequestException, ZanataMTException {

        List<String> stringsToTranslate =
                indexMap.values().stream().map(TypeString::getValue)
                        .collect(toList());

        List<String> translatedStrings = persistentTranslationService
                .translate(stringsToTranslate, srcLocale, transLocale,
                        backendID, mediaType);

        assert stringsToTranslate.size() == translatedStrings.size();
        return translatedStrings;
    }

    /**
     * Both translatedStrings and indexStringMap has the same order of entries.
     *
     * This method iterate values in translateStrings,
     * and insert it into results by position which is the index order in indexStringMap
     *
     * @param translatedStrings
     * @param indexStringMap
     * @param results
     * @param mediaType
     */
    private void transferToResults(List<String> translatedStrings,
            LinkedHashMap<Integer, TypeString> indexStringMap,
            List<TypeString> results, String mediaType) {
        int index = 0;
        for (String translatedString : translatedStrings) {
            Map.Entry<Integer, TypeString>
                    entry = Iterables.get(indexStringMap.entrySet(), index);
            results.set(entry.getKey(),
                    new TypeString(translatedString, mediaType,
                            entry.getValue().getMetadata()));
            index++;
        }
    }

    public MediaType getMediaType(String mediaType) throws BadRequestException {
        if (isMediaTypeSupported(mediaType)) {
            return MediaType.valueOf(mediaType);
        }
        throw new BadRequestException("Unsupported media type:" + mediaType);
    }

    public boolean isMediaTypeSupported(String mediaType) {
        return StringUtils.equalsAny(mediaType, MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN);
    }

    public LocaleId getMappedLocale(@Nonnull LocaleId localeId) {
        if (localeId == null) {
            return localeId;
        }
        return persistentTranslationService.getMappedLocale(localeId);
    }
}
