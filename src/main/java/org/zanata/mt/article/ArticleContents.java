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

    @Nullable
    private Map<String, Element> nonTranslatableNode;

    public ArticleContents(Document document, List<ArticleNode> articleNodes,
            Map<String, Element> nonTranslatableNode) {
        this.document = document;
        this.articleNodes = articleNodes;
        this.nonTranslatableNode = nonTranslatableNode;
    }

    Document getDocument() {
        return document;
    }

    // List of nodes which should be translated.
    // Any non-translatable parts of these nodes will be replaced by placeholders.
    public List<ArticleNode> getArticleNodes() {
        return articleNodes;
    }

    // A Map of non-translatable parts of the article, keyed by placeholder id.
    Map<String, Element> getNonTranslatableNode() {
        return nonTranslatableNode;
    }

    public String getDocumentHtml() {
        return DomUtil.extractBodyContentHTML(document);
    }

    // replace placeholder element with initial element
    public void replaceWithNonTranslatableNode() {
        if (nonTranslatableNode != null && !nonTranslatableNode.isEmpty()) {
            for (Map.Entry<String, Element> entry : nonTranslatableNode
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
