package org.zanata.mt.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.util.DomUtil;
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
     * Translate a Document
     * {@link DocumentContent}
     **/
    public DocumentContent translateDocument(DocumentContent documentContent,
            Locale srcLocale, Locale transLocale, BackendID backendID)
            throws BadRequestException, ZanataMTException {

        LinkedHashMap<Integer, TypeString> indexHTMLMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, TypeString> indexTextMap = new LinkedHashMap<>();

        //group by media type and send in batch for translation
        int index = 0;
        for (TypeString typeString: documentContent.getContents()) {
            MediaType mediaType = getMediaType(typeString.getType());

            if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                // filter out private notes, code, and non-translatable html
                String html = typeString.getValue();
                if (!DomUtil.isKCSPrivateNotes(html) &&
                        !DomUtil.isKCSCodeSection(html) &&
                        !DomUtil.isNonTranslatableNode(html)) {
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

        return new DocumentContent(results, documentContent.getUrl(),
                transLocale.getLocaleId().getId(), backendID.getId());
    }

    // translate all string values in map with given mediaType
    private List<String> translateStrings(
            LinkedHashMap<Integer, TypeString> indexMap, Locale srcLocale,
            Locale transLocale, BackendID backendID, MediaType mediaType) {

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
