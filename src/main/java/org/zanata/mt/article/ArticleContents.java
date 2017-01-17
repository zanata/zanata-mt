package org.zanata.mt.article;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.zanata.mt.article.kcs.Contents;
import org.zanata.mt.util.DomUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleContents implements Contents {

    @NotNull
    private Document document;

    // List of nodes which has modified with placeholder
    @NotNull
    private List<ArticleNode> articleNodes;

    // Map of element name with unmodified ArticleNode
    private Map<String, ArticleNode> ignoreNodeMap;

    public ArticleContents(Document document, List<ArticleNode> articleNodes,
            Map<String, ArticleNode> ignoreNodeMap) {
        this.document = document;
        this.articleNodes = articleNodes;
        this.ignoreNodeMap = ignoreNodeMap;
    }

    Document getDocument() {
        return document;
    }

    @Override
    public List<ArticleNode> getArticleNodes() {
        return articleNodes;
    }

    public Map<String, ArticleNode> getIgnoreNodeMap() {
        return ignoreNodeMap;
    }

    @Override
    public String getDocumentHtml() {
        return DomUtil.extractBodyContentHTML(document);
    }

    public void replaceNodeByName(String name, ArticleNode node) {
        Elements placeholderElements =
                document.getElementsByAttributeValue("name", name);
        if (placeholderElements != null && !placeholderElements.isEmpty()) {
            ArticleNode originalNode =
                    new ArticleNode(placeholderElements.first());
            originalNode.setHtml(node.getHtml());
        }
    }
}
