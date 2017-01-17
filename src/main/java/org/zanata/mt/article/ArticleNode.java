package org.zanata.mt.article;

import javax.validation.constraints.NotNull;

import org.jsoup.nodes.Element;
import org.zanata.mt.util.DomUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleNode {
    @NotNull
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
