package org.zanata.mt.article;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zanata.mt.util.DomUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleContents {

    @Nonnull
    private Document document;

    @Nonnull
    private List<ArticleNode> articleNodes;

    // A Map of non-translatable parts of the article, keyed by placeholder id.
    @Nullable
    private Map<String, Element> nonTranslatableElements;

    // TODO record original and translated locale, translation backendId

    public ArticleContents(Document document, List<ArticleNode> articleNodes,
            Map<String, Element> nonTranslatableElements) {
        this.document = document;
        this.articleNodes = articleNodes;
        this.nonTranslatableElements = nonTranslatableElements;
    }

    /**
     * List of nodes which should be translated.
     * Any non-translatable parts of these nodes have been replaced by placeholders.
     * @return
     */
    public List<ArticleNode> getArticleNodes() {
        return articleNodes;
    }

    /**
     * Gets the article body as HTML. Should only be called after
     * replacePlaceholdersWithOriginals().
     * @return
     */
    public String getDocumentHtml() {
        return DomUtil.extractBodyContentHTML(document);
    }

    @Nonnull
    public Document getDocument() {
        return document;
    }

    /**
     * Post-processes the document by replacing placeholder elements with original
     * non-translatable elements.
     */
    public void replacePlaceholdersWithOriginals() {
        if (nonTranslatableElements != null && !nonTranslatableElements
                .isEmpty()) {
            for (Map.Entry<String, Element> entry : nonTranslatableElements
                    .entrySet()) {
                Elements placeholderElements =
                        document.getElementsByAttributeValue("name",
                                entry.getKey());
                if (placeholderElements != null
                        && !placeholderElements.isEmpty()) {
                    Element originalNode = placeholderElements.first();
                    originalNode.replaceWith(entry.getValue());
                }
            }
        }
    }
}
