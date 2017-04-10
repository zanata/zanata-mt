package org.zanata.mt.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.TranslatableHTMLNode;
import org.zanata.mt.util.ArticleUtil;
import org.zanata.mt.exception.ZanataMTException;
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
     * Translate a Document and send in for machine translation request
     * in batch group by media type.
     *
     * {@link DocumentContent}
     **/
    public DocumentContent translateDocument(Document doc,
            DocumentContent documentContent, BackendID backendID)
            throws BadRequestException, ZanataMTException {

        LinkedHashMap<Integer, TypeString> indexHTMLMap = new LinkedHashMap<>();
        LinkedHashMap<Integer, TypeString> indexTextMap = new LinkedHashMap<>();

        Map<Integer, TranslatableHTMLNode> indexHTMLNodeMap = new HashMap<>();

        List<APIResponse> warnings = Lists.newArrayList();

        int index = 0;
        for (TypeString typeString: documentContent.getContents()) {
            MediaType mediaType = getMediaType(typeString.getType());

            if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                indexTextMap.put(index, typeString);
            } else if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                String html = typeString.getValue();

                // replace all non-translatable node with placeholder
                TranslatableHTMLNode translatableHTMLNode =
                        ArticleUtil.replaceNonTranslatableNode(index, html);
                indexHTMLNodeMap.put(index, translatableHTMLNode);
                html = translatableHTMLNode.getHtml();
                typeString.setValue(html);
                indexHTMLMap.put(index, typeString);
            }
            index++;
        }

        List<TypeString> results = Lists.newArrayList(documentContent.getContents());

        if (!indexHTMLMap.isEmpty()) {
            List<String> translatedHtmls =
                    translateStrings(doc, indexHTMLMap, backendID,
                            MediaType.TEXT_HTML_TYPE);

            replacePlaceholderAndMergeResults(translatedHtmls, indexHTMLMap,
                    indexHTMLNodeMap, results, MediaType.TEXT_HTML);
        }

        if (!indexTextMap.isEmpty()) {
            List<String> translatedStrings =
                    translateStrings(doc, indexTextMap, backendID,
                            MediaType.TEXT_PLAIN_TYPE);

            replacePlaceholderAndMergeResults(translatedStrings, indexTextMap,
                    indexHTMLNodeMap, results, MediaType.TEXT_PLAIN);
        }

        return new DocumentContent(results, documentContent.getUrl(),
                doc.getTargetLocale().getLocaleId().getId(), backendID.getId(),
                warnings);
    }

    // translate all string values in map with given mediaType
    private List<String> translateStrings(Document doc,
            LinkedHashMap<Integer, TypeString> indexMap, BackendID backendID,
            MediaType mediaType) throws BadRequestException, ZanataMTException {

        List<String> stringsToTranslate =
                indexMap.values().stream().map(TypeString::getValue)
                        .collect(toList());

        List<String> translatedStrings = persistentTranslationService
                .translate(doc, stringsToTranslate, doc.getSrcLocale(),
                        doc.getTargetLocale(), backendID, mediaType);

        assert stringsToTranslate.size() == translatedStrings.size();
        return translatedStrings;
    }

    /**
     * Both translatedStrings and indexStringMap has the same order of entries.
     *
     * This method iterate values in translateStrings,
     * and insert it into results by position which is the index order in
     * indexStringMap. If placeholders is found in the entry, replace placeholder
     * node with original html.
     *
     * @param translatedStrings
     * @param indexStringMap
     * @param results
     * @param mediaType
     */
    private void replacePlaceholderAndMergeResults(List<String> translatedStrings,
            LinkedHashMap<Integer, TypeString> indexStringMap,
            Map<Integer, TranslatableHTMLNode> indexHTMLNodeMap,
            List<TypeString> results, String mediaType) {
        int index = 0;
        for (String translatedString : translatedStrings) {
            Map.Entry<Integer, TypeString>
                    entry = Iterables.get(indexStringMap.entrySet(), index);
            if (indexHTMLNodeMap.containsKey(entry.getKey())) {
                TranslatableHTMLNode node = indexHTMLNodeMap.get(entry.getKey());
                translatedString = ArticleUtil
                        .replacePlaceholderWithNode(node.getPlaceholderIdMap(),
                                translatedString);
            }
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
