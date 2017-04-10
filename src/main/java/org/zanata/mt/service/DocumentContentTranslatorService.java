package org.zanata.mt.service;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.dto.APIResponse;
import org.zanata.mt.api.dto.DocumentContent;
import org.zanata.mt.api.dto.TypeString;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.TranslatableHTMLNode;
import org.zanata.mt.util.ArticleUtil;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.BackendID;

import com.google.common.collect.Lists;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class DocumentContentTranslatorService {
    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentContentTranslatorService.class);

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
     * Plain Text: batch up for translation requests
     * HTML: Individual string translation request
     *
     * {@link DocumentContent}
     **/
    public DocumentContent translateDocument(Document doc,
            DocumentContent documentContent, BackendID backendID, int maxLength)
            throws BadRequestException, ZanataMTException {

        LinkedHashMap<Integer, TypeString> indexTextMap = new LinkedHashMap<>();

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
                    // ignore plain text when it is more than max length
                    addWarnings(source, warnings, maxLength);
                }
            } else if (mediaType.equals(MediaType.TEXT_HTML_TYPE)) {
                // put placeholder on all non-translatable html
                TranslatableHTMLNode translatableHTMLNode =
                        ArticleUtil.replaceNonTranslatableNode(index, source);
                String html = translatableHTMLNode.getHtml();
                String translatedString;

                if (html.length() <= maxLength) {
                    translatedString = translateString(doc, html, backendID, mediaType);
                } else {
                    Element root = ArticleUtil.wrapHTML(html).children().first();
                    Element content = root.children().first();
                    translateHTMLElement(doc, backendID, mediaType, maxLength,
                            content, warnings);
                    translatedString = root.children().first().outerHtml();
                }
                // replace placeholder with original node
                translatedString = ArticleUtil
                        .replacePlaceholderWithNode(
                                translatableHTMLNode.getPlaceholderIdMap(),
                                translatedString);

                typeString.setValue(translatedString);
            }
            index++;
        }

        if (!indexTextMap.isEmpty()) {
            translateStringsInBatch(doc, backendID, MediaType.TEXT_PLAIN_TYPE,
                    maxLength, indexTextMap, results);
        }

        return new DocumentContent(results, documentContent.getUrl(),
                doc.getTargetLocale().getLocaleId().getId(), backendID.getId(),
                warnings);
    }

    /**
     * Translate strings with batch of maxLength
     */
    private void translateStringsInBatch(Document doc, BackendID backendID,
            MediaType mediaType, int maxLength,
            LinkedHashMap<Integer, TypeString> indexTextMap,
            List<TypeString> results) {
        List<String> batchedStrings = Lists.newArrayList();
        List<Integer> indexOrderList = Lists.newArrayList();

        int charCount = 0;
        Iterator iter = indexTextMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, TypeString> entry =
                    (Map.Entry<Integer, TypeString>) iter.next();
            String stringToTranslate = entry.getValue().getValue();
            if (charCount + stringToTranslate.length() > maxLength) {
                translateStrings(doc, backendID, mediaType, batchedStrings,
                        indexOrderList, results);
                charCount = 0;
                batchedStrings.clear();
                indexOrderList.clear();
            }
            batchedStrings.add(stringToTranslate);
            indexOrderList.add(entry.getKey());
            charCount += stringToTranslate.length();
        }
        translateStrings(doc, backendID, mediaType, batchedStrings,
                indexOrderList, results);
    }

    // perform strings translation
    private void translateStrings(Document doc, BackendID backendID,
            MediaType mediaType, List<String> strings,
            List<Integer> indexOrderList, List<TypeString> results) {

        List<String> translatedStrings = persistentTranslationService
                .translate(doc, strings, doc.getSrcLocale(),
                        doc.getTargetLocale(), backendID, mediaType);
        assert translatedStrings.size() == strings.size();

        for (int index = 0; index < translatedStrings.size(); index++) {
            results.get(indexOrderList.get(index))
                    .setValue(translatedStrings.get(index));
        }
    }


    // perform single string translation
    private String translateString(Document doc, String source,
            BackendID backendID, MediaType mediaType) {
        List<String> sources = Lists.newArrayList(source);
        List<String> translated =
                persistentTranslationService
                        .translate(doc, sources,
                                doc.getSrcLocale(),
                                doc.getTargetLocale(), backendID,
                                mediaType);
        assert sources.size() == translated.size();
        return translated.get(0);
    }

    /**
     * Translate all html tree nodes from top to bottom in recursive call.
     * If parent node is being translated, child node will be skip.
     *
     * root.getAllElements().size() changes once root is being translated.
     */
    private void translateHTMLElement(Document doc, BackendID backendID,
            MediaType mediaType, int maxLength, Element root,
            List<APIResponse> warnings) {
        int size = root.getAllElements().size();
        int index = 0;

        while (index < size) {
            Element child = root.getAllElements().get(index);
            String html = child.outerHtml();
            if (html.length() <= maxLength) {
                String translated =
                        translateString(doc, html, backendID, mediaType);
                child.replaceWith(ArticleUtil.asElement(translated));
            } else if (child.children().isEmpty()) {
                addWarnings(html, warnings, maxLength);
            }
            // size changes if child node is being translated
            size = root.getAllElements().size();
            index ++;
        }
    }

    private void addWarnings(String source, List<APIResponse> warnings,
            int maxLength) {
        String warning =
                "Warning: translation skipped: String length is over " +
                        maxLength;
        LOG.warn(warning + " - " + source);
        warnings.add(new APIResponse(
                Response.Status.OK, new Exception(source), warning));
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
