package org.magpie.mt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.magpie.mt.api.dto.APIResponse;
import org.magpie.mt.api.dto.DocumentContent;
import org.magpie.mt.api.dto.TypeString;
import org.magpie.mt.model.Document;
import org.magpie.mt.model.TranslatableHTMLNode;
import org.magpie.mt.util.ArticleUtil;
import org.magpie.mt.exception.MTException;
import org.magpie.mt.model.BackendID;

import com.google.common.collect.Lists;
import org.magpie.mt.util.SegmentString;
import org.magpie.mt.util.ShortString;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class DocumentContentTranslatorService {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentContentTranslatorService.class);

    // set MT category for translation
    private static final String CATEGORY = "tech";

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
     * Translate a Document and send in for machine translation request.
     *
     * If string is no longer than maxLength, it will be send as a batch according
     * to the type.
     *
     * If a string is longer than maxLength,
     * Plain Text: try to segment it and send it as a single MT request. Will
     * be ignored if cannot be segmented.
     * HTML: try to run through the node tree and translate. Will be ignored if
     * cannot be broken down.
     *
     * {@link DocumentContent}
     **/
    public DocumentContent translateDocument(Document doc,
            DocumentContent documentContent, BackendID backendID)
            throws BadRequestException, MTException {
        Map<Integer, TypeString> indexTextMap = new LinkedHashMap<>();
        Map<Integer, TypeString> indexHTMLMap = new LinkedHashMap<>();
        int maxLength = persistentTranslationService.getMaxLength(backendID);

        List<APIResponse> warnings = Lists.newArrayList();
        List<TypeString> results =
                Lists.newArrayList(documentContent.getContents());

        int index = 0;
        for (TypeString typeString: results) {
            MediaType mediaType = getMediaType(typeString.getType());
            String source = typeString.getValue();
            if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)) {
                if (source.length() <= maxLength) {
                    indexTextMap.put(index, typeString);
                } else {
                    String translatedString =
                            translateLargeString(doc, backendID,
                                    mediaType, source, maxLength, warnings);
                   typeString.setValue(translatedString);
                }
            } else if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                // put placeholder on all non-translatable html
                TranslatableHTMLNode translatableHTMLNode =
                        ArticleUtil.replaceNonTranslatableNode(index, source);
                String html = translatableHTMLNode.getHtml();

                if (html.length() <= maxLength) {
                    indexHTMLMap.put(index, typeString);
                } else {
                    String translatedString =
                            translateLargeHTMLElement(doc, backendID, mediaType,
                                    maxLength, html, warnings);
                    // replace placeholder with original node
                    translatedString = ArticleUtil
                            .replacePlaceholderWithNode(
                                    translatableHTMLNode.getPlaceholderIdMap(),
                                    translatedString);
                    typeString.setValue(translatedString);
                }
            }
            index++;
        }

        if (!indexTextMap.isEmpty()) {
            translateAndMergeStringsInBatch(doc, backendID,
                    MediaType.TEXT_PLAIN_TYPE,
                    maxLength, indexTextMap, results);
        }

        if (!indexHTMLMap.isEmpty()) {
            translateAndMergeStringsInBatch(doc, backendID,
                    MediaType.TEXT_HTML_TYPE,
                    maxLength, indexHTMLMap, results);
        }
        return new DocumentContent(results, documentContent.getUrl(),
                doc.getToLocale().getLocaleCode().getId(), backendID.getId(),
                warnings);
    }

    /**
     * Translate strings with batch of maxLength
     */
    private String translateLargeString(Document doc, BackendID backendID,
            MediaType mediaType, String source, int maxLength,
            List<APIResponse> warnings) {
        List<String> segmentedStrings =
                SegmentString.segmentString(source,
                        Optional.of(doc.getFromLocale().getLocaleCode()));
        List<String> results = new ArrayList<>(segmentedStrings);

        List<String> batchedStrings = Lists.newArrayList();
        List<Integer> indexOrderList = Lists.newArrayList();
        List<String> translatedStrings = Lists.newArrayList();
        int charCount = 0;
        for (int index = 0; index < segmentedStrings.size(); index++) {
            String string = segmentedStrings.get(index);
            // ignore string if length is longer the maxLength
            if (string.length() > maxLength) {
                addMaxLengthWarnings(string, warnings, maxLength);
                continue;
            }
            if (charCount + string.length() > maxLength) {
                List<String> translated = persistentTranslationService
                        .translate(doc, batchedStrings, doc.getFromLocale(),
                                doc.getToLocale(), backendID, mediaType,
                                Optional.of(CATEGORY));
                translatedStrings.addAll(translated);
                assert batchedStrings.size() == translated.size();
                charCount = 0;
                batchedStrings.clear();
            }
            batchedStrings.add(string);
            indexOrderList.add(index);
            charCount += string.length();
        }
        if (!batchedStrings.isEmpty()) {
            List<String> translated = persistentTranslationService
                    .translate(doc, batchedStrings, doc.getFromLocale(),
                            doc.getToLocale(), backendID, mediaType,
                            Optional.of(CATEGORY));
            translatedStrings.addAll(translated);
            assert batchedStrings.size() == translated.size();
        }

        for (int index = 0; index < translatedStrings.size(); index++) {
            results.set(indexOrderList.get(index), translatedStrings.get(index));
        }
        return String.join("", results);
    }

    /**
     * Translate strings with batch of maxLength
     */
    private void translateAndMergeStringsInBatch(Document doc, BackendID backendID,
            MediaType mediaType, int maxLength,
            Map<Integer, TypeString> indexTypeStringMap,
            List<TypeString> results) {
        List<String> batchedStrings = Lists.newArrayList();
        List<Integer> indexOrderList = Lists.newArrayList();
        Map<Integer, TranslatableHTMLNode> htmlNodeCache = new HashMap<>();

        int charCount = 0;
        Iterator<Map.Entry<Integer, TypeString>> iter =
                indexTypeStringMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, TypeString> entry = iter.next();
            String stringToTranslate = entry.getValue().getValue();
            if (mediaType == MediaType.TEXT_HTML_TYPE) {
                int index = entry.getKey();
                TranslatableHTMLNode translatableHTMLNode =
                        ArticleUtil.replaceNonTranslatableNode(index,
                                stringToTranslate);
                htmlNodeCache.put(index, translatableHTMLNode);
                stringToTranslate = translatableHTMLNode.getHtml();
            }

            if (charCount + stringToTranslate.length() > maxLength) {
                translateAndMergeStrings(doc, backendID, mediaType, batchedStrings,
                        indexOrderList, results);
                charCount = 0;
                batchedStrings.clear();
                indexOrderList.clear();
            }
            batchedStrings.add(stringToTranslate);
            indexOrderList.add(entry.getKey());
            charCount += stringToTranslate.length();
        }
        translateAndMergeStrings(doc, backendID, mediaType, batchedStrings,
                indexOrderList, results);

        // restore placeholder into html
        if (!htmlNodeCache.isEmpty()) {
            for (Map.Entry<Integer, TranslatableHTMLNode> entry: htmlNodeCache.entrySet()) {
                int index = entry.getKey();
                TypeString typeString = results.get(index);
                String translatedString = ArticleUtil
                        .replacePlaceholderWithNode(
                                entry.getValue().getPlaceholderIdMap(),
                                typeString.getValue());
                typeString.setValue(translatedString);
                results.set(index, typeString);
            }
        }
    }

    // perform translation on list of string and merge into results
    private void translateAndMergeStrings(Document doc, BackendID backendID,
            MediaType mediaType, List<String> strings,
            List<Integer> indexOrderList, List<TypeString> results) {

        List<String> translatedStrings = persistentTranslationService
                .translate(doc, strings, doc.getFromLocale(),
                        doc.getToLocale(), backendID, mediaType,
                        Optional.of(CATEGORY));
        assert translatedStrings.size() == strings.size();

        for (int index = 0; index < translatedStrings.size(); index++) {
            results.get(indexOrderList.get(index))
                    .setValue(translatedStrings.get(index));
        }
    }

    /**
     * Translate all html tree nodes from top to bottom.
     * If parent node is being translated,
     * its child nodes will not be translated again.
     *
     * root.getAllElements().size() changes once root is being translated.
     */
    private String translateLargeHTMLElement(Document doc, BackendID backendID,
            MediaType mediaType, int maxLength, String source,
            List<APIResponse> warnings) {

        List<Element> contents =
                ArticleUtil.unwrapAsElements(ArticleUtil.wrapHTML(source));

        for (int i = 0; i < contents.size(); i++) {
            Element content = contents.get(i);
            int size = content.getAllElements().size();
            int index = 0;

            while (index < size) {
                Element child = content.getAllElements().get(index);
                String html = child.outerHtml();
                if (html.length() <= maxLength) {
                    List<String> translated =
                            persistentTranslationService
                                    .translate(doc, Lists.newArrayList(html),
                                            doc.getFromLocale(),
                                            doc.getToLocale(), backendID,
                                            mediaType, Optional.of(CATEGORY));
                    assert translated.size() == 1;
                    Element replacement = ArticleUtil.asElement(translated.get(0));
                    if (child == content) {
                        //replace this item in contents list, exit while loop
                        contents.set(i, replacement);
                        break;
                    } else {
                        child.replaceWith(replacement);
                    }
                } else {
                    // show warning if there is no more children under this node
                    addMaxLengthWarnings(html, warnings, maxLength);
                }
                // size changes if child node is being translated
                size = content.getAllElements().size();
                index++;
            }
        }
        return contents.stream().map(node -> node.outerHtml())
                .collect(Collectors.joining());
    }

    private void addMaxLengthWarnings(String source, List<APIResponse> warnings,
            int maxLength) {
        String title =
                "Warning: translation skipped: String length is over " +
                        maxLength;
        String shortenString = ShortString.shorten(source);
        LOG.warn(title + " - " + shortenString);
        warnings.add(new APIResponse(
                Response.Status.BAD_REQUEST, new Exception(shortenString),
                title));
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
