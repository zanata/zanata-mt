package org.zanata.magpie.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.api.dto.APIResponse;
import org.zanata.magpie.api.dto.DocumentContent;
import org.zanata.magpie.api.dto.TypeString;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.util.ArticleUtil;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;

import com.google.common.collect.ImmutableList;
import org.zanata.magpie.util.SegmentString;
import org.zanata.magpie.util.ShortString;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class DocumentContentTranslatorService {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentContentTranslatorService.class);

    // set MT category for translation
    private static final String CATEGORY = "tech";

    private PersistentTranslationService persistentTranslationService;

    @SuppressWarnings({"unused", "uninitialized"})
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
        Map<Integer, TypeString> indexXMLMap = new LinkedHashMap<>();
        int maxLength = persistentTranslationService.getMaxLength(backendID);

        List<APIResponse> warnings = new ArrayList<>();
        List<TypeString> typeStrings =
                new ArrayList<>(documentContent.getContents());

        // counts short strings (less than maxLength) which are translated
        // in a batch at the end
        int index = 0;
        for (TypeString typeString: typeStrings) {
            MediaType mediaType = getMediaType(typeString.getType());
            String source = typeString.getValue();
            if (mediaType.equals(MediaType.TEXT_PLAIN_TYPE)
                    && !StringUtils.isBlank(source)) {
                if (source.length() <= maxLength) {
                    indexTextMap.put(index, typeString);
                } else {
                    StringTranslationResult result =
                            translateLargeString(doc, backendID,
                                    StringType.TEXT_PLAIN, source, maxLength);
                    typeString.setValue(result.getTranslation());
                    warnings.addAll(result.getWarnings());
                }
            } else if (mediaType.equals(MediaType.TEXT_HTML_TYPE) ||
                    mediaType.equals(MediaType.TEXT_XML_TYPE)) {
                boolean xml = mediaType.equals(MediaType.TEXT_XML_TYPE);

                // put placeholders in all non-translatable elements
                TranslatableNodeList translatableNodeList = xml ?
                        ArticleUtil.replaceNonTranslatableNodeXML(index, source) :
                        ArticleUtil.replaceNonTranslatableNodeHTML(index, source);
                String html = translatableNodeList.getHtml();
                if (html.length() <= maxLength) {
                    Map<Integer, TypeString> indexMap = xml ? indexXMLMap : indexHTMLMap;
                    indexMap.put(index, typeString);
                } else {
                    Function<String, Element> toElement = xml ? ArticleUtil::wrapXML : ArticleUtil::wrapHTML;
                    StringTranslationResult result =
                            translateLargeElement(doc, backendID, mediaType,
                                    maxLength, html, toElement);
                    String translationWithPlaceholders = result.getTranslation();

                    // replace placeholder with original node
                    String translatedString =
                            removePlaceholders(translationWithPlaceholders,
                                    translatableNodeList, toElement);
                    typeString.setValue(translatedString);
                    warnings.addAll(result.getWarnings());
                }
            }
            index++;
        }

        translateAndMergeStringsInBatch(doc, backendID, StringType.TEXT_PLAIN,
                maxLength, indexTextMap, typeStrings);
        translateAndMergeStringsInBatch(doc, backendID, StringType.HTML,
                maxLength, indexHTMLMap, typeStrings);
        translateAndMergeStringsInBatch(doc, backendID, StringType.XML,
                maxLength, indexXMLMap, typeStrings);
        return new DocumentContent(typeStrings, documentContent.getUrl(),
                doc.getToLocale().getLocaleCode().getId(), backendID.getId(),
                warnings);
    }

    private String removePlaceholders(String translationWithPlaceholders,
            TranslatableNodeList translatableNodeList,
            Function<String, Element> toElement) {
        return ArticleUtil.replacePlaceholderWithNode(
                translatableNodeList.getPlaceholderIdMap(),
                translationWithPlaceholders, toElement);
    }

    /**
     * Translate strings with batch of maxLength
     */
    private StringTranslationResult translateLargeString(Document doc, BackendID backendID,
            StringType stringType, String source, int maxLength) {
        List<APIResponse> warnings = new ArrayList<>();
        List<String> segmentedStrings =
                SegmentString.segmentString(source,
                        Optional.of(doc.getFromLocale().getLocaleCode()));
        List<String> results = new ArrayList<>(segmentedStrings);

        List<String> batchedStrings = new ArrayList<>();
        List<Integer> indexOrderList = new ArrayList<>();
        List<String> translatedStrings = new ArrayList<>();
        int charCount = 0;
        for (int index = 0; index < segmentedStrings.size(); index++) {
            String string = segmentedStrings.get(index);
            // ignore string if length is longer the maxLength
            if (string.length() > maxLength) {
                warnings.add(maxLengthWarning(string, maxLength));
                continue;
            }
            if (charCount + string.length() > maxLength) {
                List<String> translated = persistentTranslationService
                        .translate(doc, batchedStrings, doc.getFromLocale(),
                                doc.getToLocale(), backendID, stringType,
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
                            doc.getToLocale(), backendID, stringType,
                            Optional.of(CATEGORY));
            translatedStrings.addAll(translated);
            assert batchedStrings.size() == translated.size();
        }

        for (int index = 0; index < translatedStrings.size(); index++) {
            results.set(indexOrderList.get(index), translatedStrings.get(index));
        }
        return new StringTranslationResult(String.join("", results), warnings);
    }

    /**
     * Translate strings with batch of maxLength
     * @param docStringType text/plain, text/html or text/xml
     */
    private void translateAndMergeStringsInBatch(Document doc, BackendID backendID,
            StringType docStringType, int maxLength,
            Map<Integer, TypeString> indexTypeStringMap,
            List<TypeString> results) {
        if (indexTypeStringMap.isEmpty()) return;
        List<String> batchedStrings = new ArrayList<>();
        List<Integer> indexOrderList = new ArrayList<>();
        // html or xml nodes
        Map<Integer, TranslatableNodeList> nodeCache = new HashMap<>();

        int charCount = 0;
        for (Map.Entry<Integer, TypeString> entry : indexTypeStringMap
                .entrySet()) {
            int index = entry.getKey();
            TypeString typeString = entry.getValue();

            String stringToTranslate = typeString.getValue();
            String stringType = typeString.getType();
            if (stringType.equals(MediaType.TEXT_HTML)) {
                TranslatableNodeList translatableNodeList =
                        ArticleUtil.replaceNonTranslatableNodeHTML(index,
                                stringToTranslate);
                nodeCache.put(index, translatableNodeList);
                stringToTranslate = translatableNodeList.getHtml();
            } else if (stringType.equals(MediaType.TEXT_XML)) {
                TranslatableNodeList translatableNodeList =
                        ArticleUtil.replaceNonTranslatableNodeXML(index,
                                stringToTranslate);
                nodeCache.put(index, translatableNodeList);
                stringToTranslate = translatableNodeList.getHtml();
            }

            if (charCount + stringToTranslate.length() > maxLength) {
                translateAndMergeStrings(doc, backendID,
                        StringType.fromMediaType(stringType), batchedStrings,
                        indexOrderList, results);
                charCount = 0;
                batchedStrings.clear();
                indexOrderList.clear();
            }
            batchedStrings.add(stringToTranslate);
            indexOrderList.add(entry.getKey());
            charCount += stringToTranslate.length();
        }
        translateAndMergeStrings(doc, backendID, docStringType, batchedStrings,
                indexOrderList, results);

        // restore placeholder nodes with original values in html/xml
        if (!nodeCache.isEmpty()) {
            for (Map.Entry<Integer, TranslatableNodeList> entry: nodeCache.entrySet()) {
                int index = entry.getKey();
                TypeString typeString = results.get(index);
                Map<String, Node> placeholderIdMap =
                        entry.getValue().getPlaceholderIdMap();
                String translatedString;
                switch (typeString.getType()) {
                    case MediaType.TEXT_HTML:
                        translatedString = ArticleUtil
                                .replacePlaceholderWithNode(
                                        placeholderIdMap,
                                        typeString.getValue(),
                                        ArticleUtil::wrapHTML);
                        break;
                    case MediaType.TEXT_XML:
                        translatedString = ArticleUtil
                                .replacePlaceholderWithNode(
                                        placeholderIdMap,
                                        typeString.getValue(),
                                        ArticleUtil::wrapXML);
                        break;
                    default:
                        throw new RuntimeException();
                }

                typeString.setValue(translatedString);
                results.set(index, typeString);
            }
        }
    }

    // perform translation on list of string and merge into results
    private void translateAndMergeStrings(Document doc, BackendID backendID,
            StringType stringType, List<String> strings,
            List<Integer> indexOrderList, List<TypeString> results) {

        List<String> translatedStrings = persistentTranslationService
                .translate(doc, strings, doc.getFromLocale(),
                        doc.getToLocale(), backendID, stringType,
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
     * @param mediaType text/plain, text/html or text/xml
     */
    private StringTranslationResult translateLargeElement(Document doc, BackendID backendID,
            MediaType mediaType, int maxLength, String source,
            Function<String, Element> toElement) {

        List<APIResponse> warnings = new ArrayList<>();
        List<Node> contents =
                ArticleUtil.unwrapAsElements(toElement.apply(source));

        for (int contentIndex = 0; contentIndex < contents.size(); contentIndex++) {
            Node content = contents.get(contentIndex);
            // if content is a (large) TextNode ie no child nodes:
            if (content instanceof TextNode) {
                warnings.add(maxLengthWarning(source, maxLength));
            } else {
                translateChildNodes(doc, backendID, mediaType, maxLength,
                        toElement, warnings, content);
            }
        }
        String translation = contents.stream()
                .map(Node::outerHtml)
                .collect(Collectors.joining());
        return new StringTranslationResult(translation, warnings);
    }

    private void translateChildNodes(Document doc, BackendID backendID,
            MediaType mediaType,
            int maxLength, Function<String, Element> toElement,
            List<APIResponse> warnings, Node content) {
        int childCount = content.childNodeSize();
        int childIndex = 0;

        while (childIndex < childCount) {
            Node child = content.childNode(childIndex);
            String html = child.outerHtml();
            if (html.length() <= maxLength) {
                List<String> translated =
                        persistentTranslationService
                                .translate(doc, ImmutableList.of(html),
                                        doc.getFromLocale(),
                                        doc.getToLocale(), backendID,
                                        StringType.fromMediaType(mediaType),
                                        Optional.of(CATEGORY));
                assert translated.size() == 1;
                Node replacement = ArticleUtil
                        .asElement(translated.get(0), toElement);
                if (replacement != null) {
                    child.replaceWith(replacement);
                }
            } else {
                // show warning if there are no more children under this node
                warnings.add(maxLengthWarning(html, maxLength));
            }
            // size changes if child node is being translated
            childCount = content.childNodeSize();
            childIndex++;
        }
    }

    private APIResponse maxLengthWarning(String source,
            int maxLength) {
        String title =
                "Warning: translation skipped: String length is over " +
                        maxLength;
        String shortenString = ShortString.shorten(source);
        LOG.warn(title + " - " + shortenString);
        return new APIResponse(Response.Status.BAD_REQUEST,
                new Exception(shortenString), title);
    }

    public MediaType getMediaType(String mediaType) throws BadRequestException {
        if (isMediaTypeSupported(mediaType)) {
            return MediaType.valueOf(mediaType);
        }
        throw new BadRequestException("Unsupported media type: " + mediaType);
    }

    public boolean isMediaTypeSupported(String mediaType) {
        return StringUtils.equalsAny(mediaType, MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN, MediaType.TEXT_XML);
    }
}
