package org.zanata.mt.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.DocumentContent;
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

    private PersistentTranslationService persistentTranslationService;

    @SuppressWarnings("unused")
    public DocumentContentTranslatorService() {
    }

    @Inject
    public DocumentContentTranslatorService(
            PersistentTranslationService persistentTranslationService) {
        this.persistentTranslationService = persistentTranslationService;
    }

    /**
     * Replace code-section and private-notes section with placeholder.
     * Append warning in the response.
     */
    private void processPrivateNotesAndCodeSection(TypeString typeString,
            Map<String, String> nonTranslatePlaceholderMap,
            List<APIResponse> warnings, int index) {

        String indexStr = String.valueOf(index);
        String html = typeString.getValue();

        if (ArticleUtil.containsPrivateNotes(html)) {
            // cache original html with index
            nonTranslatePlaceholderMap.put(indexStr, html);
            String nonTranslatableHTML =
                    ArticleUtil.generateNonTranslatableHtml(indexStr);
            // replace private-notes with placeholder
            ArticleUtil.replacePrivateNotes(typeString,
                    nonTranslatableHTML);
            warnings.add(new APIResponse(Response.Status.OK,
                    "String contains private notes section:" + html));
        } else if (ArticleUtil.containsKCSCodeSection(html)) {
            // cache original html with index
            nonTranslatePlaceholderMap.put(indexStr, html);
            String nonTranslatableHTML =
                    ArticleUtil.generateNonTranslatableHtml(indexStr);
            // replace code-section with placeholder
            ArticleUtil.replaceKCSCodeSection(typeString,
                    nonTranslatableHTML);
            warnings.add(new APIResponse(Response.Status.OK,
                    "String contains KCS code section:" + html));
        }
    }

    /**
     * Translate a Document and send in for machine translation request
     * in batch group by media type.
     *
     * Any HTML node that is not translatable {@link ArticleUtil#isNonTranslatableNode(String)}
     * will be excluded from translation request.
     *
     * For private-notes section {@link ArticleUtil#containsPrivateNotes(String)}
     * and code-section {@link ArticleUtil#containsKCSCodeSection(String)},
     * a placeholder will replace the section before sending out request.
     * A warning message will be included in the response.
     *
     * {@link DocumentContent}
     **/
    public DocumentContent translateDocument(DocumentContent documentContent,
            Locale srcLocale, Locale transLocale, BackendID backendID)
            throws BadRequestException, ZanataMTException {

        LinkedHashMap<Integer, TypeString> indexHTMLMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, TypeString> indexTextMap = new LinkedHashMap<>();

        Map<String, String> nonTranslatePlaceholderMap = new HashMap<>();
        List<APIResponse> warnings = null;

        int index = 0;
        for (TypeString typeString: documentContent.getContents()) {
            MediaType mediaType = getMediaType(typeString.getType());

            if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                String html = typeString.getValue();
                // filter out non-translatable html
                if (!ArticleUtil.isNonTranslatableNode(html)) {
                    if (ArticleUtil.containsPrivateNotes(html) ||
                            ArticleUtil.containsKCSCodeSection(html)) {
                        //replace with placeholder in the html node
                        warnings = Lists.newArrayList();
                        processPrivateNotesAndCodeSection(typeString,
                                nonTranslatePlaceholderMap, warnings, index);
                    }
                    indexHTMLMap.put(index, typeString);
                }
            } else if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                indexTextMap.put(index, typeString);
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

        // replace placeholder with original content according to index
        for (Map.Entry<String, String> entry : nonTranslatePlaceholderMap
                .entrySet()) {
            String indexStr = entry.getKey();
            index = Integer.valueOf(indexStr);

            String originalHtml = entry.getValue();
            TypeString translatedTypeString = results.get(index);
            ArticleUtil.replaceNodeById(indexStr, originalHtml, translatedTypeString);
            results.set(index, translatedTypeString);
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
}
