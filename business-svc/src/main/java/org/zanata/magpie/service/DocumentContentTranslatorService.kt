package org.zanata.magpie.service

import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Optional
import java.util.stream.Collectors
import javax.ejb.Stateless
import javax.inject.Inject
import javax.ws.rs.BadRequestException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.zanata.magpie.api.dto.APIResponse
import org.zanata.magpie.api.dto.DocumentContent
import org.zanata.magpie.api.dto.TypeString
import org.zanata.magpie.model.Document
import org.zanata.magpie.model.StringType
import org.zanata.magpie.util.ArticleUtil
import org.zanata.magpie.exception.MTException
import org.zanata.magpie.model.BackendID

import com.google.common.collect.ImmutableList
import org.zanata.magpie.api.dto.LocaleCode
import org.zanata.magpie.util.SegmentString
import org.zanata.magpie.util.ShortString

/**
 * @author Alex Eng [aeng@redhat.com](mailto:aeng@redhat.com)
 */
@Stateless
class DocumentContentTranslatorService @Inject constructor(
        private val persistentTranslationService: PersistentTranslationService) {

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
     * [DocumentContent]
     */
    @Throws(BadRequestException::class, MTException::class)
    fun translateDocument(doc: Document, documentContent: DocumentContent,
            backendID: BackendID): DocumentContent {
        val indexTextMap = LinkedHashMap<Int, TypeString>()
        val indexHTMLMap = LinkedHashMap<Int, TypeString>()
        val indexXMLMap = LinkedHashMap<Int, TypeString>()
        val maxLength = persistentTranslationService.getMaxLength(backendID)

        val warnings = ArrayList<APIResponse>()
        val typeStrings = ArrayList(documentContent.contents)

        // counts short strings (less than maxLength) which are translated
        // in a batch at the end
        for ((index, typeString) in typeStrings.withIndex()) {
            val mediaType = getMediaType(typeString.type)
            val source = typeString.value
            if (mediaType == MediaType.TEXT_PLAIN_TYPE) {
                if (source.length <= maxLength) {
                    indexTextMap[index] = typeString
                } else {
                    val (translation, warnings1) = translateLargeString(doc, backendID,
                            StringType.TEXT_PLAIN, source, maxLength)
                    typeString.value = translation
                    warnings.addAll(warnings1)
                }
            } else if (mediaType == MediaType.TEXT_HTML_TYPE || mediaType == MediaType.TEXT_XML_TYPE) {
                val xml = mediaType == MediaType.TEXT_XML_TYPE

                // put placeholders in all non-translatable elements
                val translatableNodeList = if (xml)
                    ArticleUtil.replaceNonTranslatableNodeXML(index, source)
                else
                    ArticleUtil.replaceNonTranslatableNodeHTML(index, source)
                val html = translatableNodeList.html
                if (html.length <= maxLength) {
                    val indexMap = if (xml) indexXMLMap else indexHTMLMap
                    indexMap[index] = typeString
                } else {
                    val toElement = if (xml) ArticleUtil::wrapXML else ArticleUtil::wrapHTML
                    val (translationWithPlaceholders, warnings1) = translateLargeElement(doc, backendID, mediaType,
                            maxLength, html, toElement)

                    // replace placeholder with original node
                    val translatedString = removePlaceholders(translationWithPlaceholders,
                            translatableNodeList, toElement)
                    typeString.value = translatedString
                    warnings.addAll(warnings1)
                }
            }
        }

        translateAndMergeStringsInBatch(doc, backendID, StringType.TEXT_PLAIN,
                maxLength, indexTextMap, typeStrings)
        translateAndMergeStringsInBatch(doc, backendID, StringType.HTML,
                maxLength, indexHTMLMap, typeStrings)
        translateAndMergeStringsInBatch(doc, backendID, StringType.XML,
                maxLength, indexXMLMap, typeStrings)
        return DocumentContent(typeStrings, documentContent.url,
                doc.toLocale.localeCode.id, backendID.id,
                warnings)
    }

    private fun removePlaceholders(translationWithPlaceholders: String,
            translatableNodeList: TranslatableNodeList,
            toElement: (String) -> Element): String {
        return ArticleUtil.replacePlaceholderWithNode(
                translatableNodeList.placeholderIdMap,
                translationWithPlaceholders, toElement)
    }

    /**
     * Translate strings with batch of maxLength
     */
    private fun translateLargeString(doc: Document, backendID: BackendID,
            stringType: StringType, source: String, maxLength: Int): StringTranslationResult {
        val warnings = ArrayList<APIResponse>()
        val segmentedStrings = SegmentString.segmentString(source,
                Optional.of<LocaleCode>(doc.fromLocale.localeCode))
        val results = ArrayList(segmentedStrings)

        val batchedStrings = ArrayList<String>()
        val indexOrderList = ArrayList<Int>()
        val translatedStrings = ArrayList<String>()
        var charCount = 0
        for (index in segmentedStrings.indices) {
            val string = segmentedStrings[index]
            // ignore string if length is longer the maxLength
            if (string.length > maxLength) {
                warnings.add(maxLengthWarning(string, maxLength))
                continue
            }
            if (charCount + string.length > maxLength) {
                val translated = persistentTranslationService
                        .translate(doc, batchedStrings, doc.fromLocale,
                                doc.toLocale, backendID, stringType,
                                Optional.of(CATEGORY))
                translatedStrings.addAll(translated)
                assert(batchedStrings.size == translated.size)
                charCount = 0
                batchedStrings.clear()
            }
            batchedStrings.add(string)
            indexOrderList.add(index)
            charCount += string.length
        }
        if (!batchedStrings.isEmpty()) {
            val translated = persistentTranslationService
                    .translate(doc, batchedStrings, doc.fromLocale,
                            doc.toLocale, backendID, stringType,
                            Optional.of(CATEGORY))
            translatedStrings.addAll(translated)
            assert(batchedStrings.size == translated.size)
        }

        for (index in translatedStrings.indices) {
            results[indexOrderList[index]] = translatedStrings[index]
        }
        return StringTranslationResult(results.joinToString(""), warnings)
    }

    /**
     * Translate strings with batch of maxLength
     * @param docStringType text/plain, text/html or text/xml
     */
    private fun translateAndMergeStringsInBatch(doc: Document, backendID: BackendID,
            docStringType: StringType, maxLength: Int,
            indexTypeStringMap: Map<Int, TypeString>,
            results: MutableList<TypeString>) {
        if (indexTypeStringMap.isEmpty()) return
        val batchedStrings = ArrayList<String>()
        val indexOrderList = ArrayList<Int>()
        // html or xml nodes
        val nodeCache = HashMap<Int, TranslatableNodeList>()

        var charCount = 0
        for ((index, typeString) in indexTypeStringMap) {

            var stringToTranslate = typeString.value
            val stringType = typeString.type
            if (stringType == MediaType.TEXT_HTML) {
                val translatableNodeList = ArticleUtil.replaceNonTranslatableNodeHTML(index,
                        stringToTranslate)
                nodeCache[index] = translatableNodeList
                stringToTranslate = translatableNodeList.html
            } else if (stringType == MediaType.TEXT_XML) {
                val translatableNodeList = ArticleUtil.replaceNonTranslatableNodeXML(index,
                        stringToTranslate)
                nodeCache[index] = translatableNodeList
                stringToTranslate = translatableNodeList.html
            }

            if (charCount + stringToTranslate.length > maxLength) {
                translateAndMergeStrings(doc, backendID,
                        StringType.fromMediaType(stringType), batchedStrings,
                        indexOrderList, results)
                charCount = 0
                batchedStrings.clear()
                indexOrderList.clear()
            }
            batchedStrings.add(stringToTranslate)
            indexOrderList.add(index)
            charCount += stringToTranslate.length
        }
        translateAndMergeStrings(doc, backendID, docStringType, batchedStrings,
                indexOrderList, results)

        // restore placeholder nodes with original values in html/xml
        for ((index, value) in nodeCache) {
            val typeString = results[index]
            val placeholderIdMap = value.placeholderIdMap
            val translatedString = when (typeString.type) {
                MediaType.TEXT_HTML -> ArticleUtil.replacePlaceholderWithNode(
                        placeholderIdMap, typeString.value, ArticleUtil::wrapHTML
                )
                MediaType.TEXT_XML -> ArticleUtil.replacePlaceholderWithNode(
                        placeholderIdMap, typeString.value, ArticleUtil::wrapXML
                )
                else -> throw RuntimeException()
            }
            typeString.value = translatedString
            results[index] = typeString
        }
    }

    // perform translation on list of string and merge into results
    private fun translateAndMergeStrings(doc: Document, backendID: BackendID,
            stringType: StringType, strings: List<String>,
            indexOrderList: List<Int>, results: List<TypeString>) {
        val translatedStrings = persistentTranslationService
                .translate(doc, strings, doc.fromLocale,
                        doc.toLocale, backendID, stringType,
                        Optional.of(CATEGORY))
        assert(translatedStrings.size == strings.size)

        for (index in translatedStrings.indices) {
            results[indexOrderList[index]].value = translatedStrings[index]
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
    private fun translateLargeElement(doc: Document, backendID: BackendID,
            mediaType: MediaType, maxLength: Int, source: String,
            toElement: (String) -> Element): StringTranslationResult {

        val warnings = ArrayList<APIResponse>()
        val contents = ArticleUtil.unwrapAsElements(toElement(source))

        // TODO try handling as Nodes, using childNode() and childNodeSize()
        for (i in contents.indices) {
            val content = contents[i]
            var size = content.allElements.size
            var index = 0

            while (index < size) {
                val child = content.allElements[index]
                val html = child.outerHtml()
                if (html.length <= maxLength) {
                    val translated = persistentTranslationService
                            .translate(doc, ImmutableList.of(html),
                                    doc.fromLocale,
                                    doc.toLocale, backendID,
                                    StringType.fromMediaType(mediaType),
                                    Optional.of(CATEGORY))
                    assert(translated.size == 1)
                    val replacement = ArticleUtil.asElement(translated[0], toElement)
                    if (replacement != null) {
                        if (child === content) {
                            //replace this item in contents list, exit while loop
                            contents[i] = replacement
                            break
                        } else {
                            child.replaceWith(replacement)
                        }
                    }
                } else {
                    // show warning if there are no more children under this node
                    warnings.add(maxLengthWarning(html, maxLength))
                }
                // size changes if child node is being translated
                size = content.allElements.size
                index++
            }
        }
        val translation = contents.joinToString("", transform = Element::outerHtml)
        return StringTranslationResult(translation, warnings)
    }

    private fun maxLengthWarning(source: String,
            maxLength: Int): APIResponse {
        val title = "Warning: translation skipped: String length is over $maxLength"
        val shortenString = ShortString.shorten(source)
        LOG.warn("$title - $shortenString")
        return APIResponse(Response.Status.BAD_REQUEST,
                Exception(shortenString), title)
    }

    @Throws(BadRequestException::class)
    fun getMediaType(mediaType: String): MediaType {
        if (isMediaTypeSupported(mediaType)) {
            return MediaType.valueOf(mediaType)
        }
        throw BadRequestException("Unsupported media type: $mediaType")
    }

    fun isMediaTypeSupported(mediaType: String): Boolean {
        return StringUtils.equalsAny(mediaType, MediaType.TEXT_HTML,
                MediaType.TEXT_PLAIN, MediaType.TEXT_XML)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(DocumentContentTranslatorService::class.java)

        // set MT category for translation
        private const val CATEGORY = "tech"
    }
}
