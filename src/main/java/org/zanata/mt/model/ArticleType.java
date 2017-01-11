package org.zanata.mt.model;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ArticleType {
    public static ArticleType KCS_ARTICLE = new ArticleType("KCS_ARTICLE");

    private String type;

    public ArticleType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
