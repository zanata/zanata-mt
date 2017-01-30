package org.zanata.mt.article;

import javax.annotation.Nonnull;

import org.jsoup.nodes.Element;
import org.zanata.mt.util.DomUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleNode {
    @Nonnull
    private Element element;

    public ArticleNode(Element element) {
        this.element = element;
    }

    public String getHtml() {
        return element.outerHtml();
    }

    public void setHtml(String html) {
        Element replaceElement = DomUtil.parseAsElement(html).first();
        element.replaceWith(replaceElement);
    }
}
