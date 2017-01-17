package org.zanata.mt.article.kcs;

import java.util.List;
import java.util.Map;

import org.zanata.mt.article.ArticleNode;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public interface Contents {
    public List<ArticleNode> getArticleNodes();

    Map<String, ArticleNode> getIgnoreNodeMap();

    public String getDocumentHtml();
}
