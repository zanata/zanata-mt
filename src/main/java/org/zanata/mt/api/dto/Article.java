package org.zanata.mt.api.dto;

import java.io.Serializable;
import javax.annotation.Nullable;
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

    @Nullable
    private String locale;

    @Nullable
    private String backendId;

    @SuppressWarnings("unused")
    protected Article() {
    }

    public Article(String titleText, String contentHTML, String url,
        String articleType, String locale) {
        this(titleText, contentHTML, url, articleType, locale, null);
    }

    public Article(String titleText, String contentHTML, String url,
            String articleType, String locale, String backendId) {
        this.titleText = titleText;
        this.contentHTML = contentHTML;
        this.url = url;
        this.articleType = articleType;
        this.locale = locale;
        this.backendId = backendId;
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

    /**
     * @return locale of this Article
     */
    @Nullable
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return backendId of translated content for this Article
     */
    @Nullable
    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(@Nullable String backendId) {
        this.backendId = backendId;
    }

    @Override
    public String toString() {
        return "Article{" +
            "titleText='" + titleText + '\'' +
            ", contentHTML='" + contentHTML + '\'' +
            ", url='" + url + '\'' +
            ", articleType='" + articleType + '\'' +
            ", locale='" + locale + '\'' +
            ", backendId='" + backendId + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;

        Article article = (Article) o;

        if (getTitleText() != null ?
            !getTitleText().equals(article.getTitleText()) :
            article.getTitleText() != null) return false;
        if (getContentHTML() != null ?
            !getContentHTML().equals(article.getContentHTML()) :
            article.getContentHTML() != null) return false;
        if (getUrl() != null ? !getUrl().equals(article.getUrl()) :
            article.getUrl() != null) return false;
        if (getArticleType() != null ?
            !getArticleType().equals(article.getArticleType()) :
            article.getArticleType() != null) return false;
        if (getLocale() != null ? !getLocale().equals(article.getLocale()) :
            article.getLocale() != null) return false;
        return getBackendId() != null ?
            getBackendId().equals(article.getBackendId()) :
            article.getBackendId() == null;

    }

    @Override
    public int hashCode() {
        int result = getTitleText() != null ? getTitleText().hashCode() : 0;
        result = 31 * result +
            (getContentHTML() != null ? getContentHTML().hashCode() : 0);
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result +
            (getArticleType() != null ? getArticleType().hashCode() : 0);
        result =
            31 * result + (getLocale() != null ? getLocale().hashCode() : 0);
        result =
            31 * result +
                (getBackendId() != null ? getBackendId().hashCode() : 0);
        return result;
    }
}
