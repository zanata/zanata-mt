package org.zanata.mt.api.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * DTO for contentHTML to be translated
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class Article implements Serializable {
    private static final long serialVersionUID = 4123397809604573837L;

    private String titleText;

    private String contentHTML;

    @NotNull
    private String url;

    @NotNull
    private String articleType;

    @SuppressWarnings("unused")
    protected Article() {
    }

    public Article(String titleText, String contentHTML, String url, String articleType) {
        this.titleText = titleText;
        this.contentHTML = contentHTML;
        this.url = url;
        this.articleType = articleType;
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    public String getContentHTML() {
        return contentHTML;
    }

    public void setContentHTML(String contentHTML) {
        this.contentHTML = contentHTML;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    @Override
    public String toString() {
        return "Article{" +
            "titleText='" + titleText + '\'' +
            ", contentHTML='" + contentHTML + '\'' +
            ", url='" + url + '\'' +
            ", articleType='" + articleType + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;

        Article article = (Article) o;

        if (titleText != null ? !titleText.equals(article.titleText) :
            article.titleText != null)
            return false;
        if (contentHTML != null ? !contentHTML.equals(article.contentHTML) :
            article.contentHTML != null) return false;
        if (url != null ? !url.equals(article.url) : article.url != null)
            return false;
        return articleType != null ? articleType.equals(article.articleType) :
            article.articleType == null;

    }

    @Override
    public int hashCode() {
        int result = titleText != null ? titleText.hashCode() : 0;
        result = 31 * result + (contentHTML != null ? contentHTML.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result =
            31 * result + (articleType != null ? articleType.hashCode() : 0);
        return result;
    }
}
