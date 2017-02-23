package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * JSON entity for article translations request and response.
 * This entity is used for KCS article translations by accepting html contents.
 *
 * Used in
 * {@link org.zanata.mt.api.ArticleTranslatorResource#translate(RawArticle, LocaleId)}}
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class RawArticle implements Serializable {
    private static final long serialVersionUID = 4123397809604573837L;

    private String titleText;

    private String contentHTML;

    private String url;

    private String articleType;

    private String locale;

    private String backendId;

    @SuppressWarnings("unused")
    protected RawArticle() {
    }

    public RawArticle(String titleText, String contentHTML, String url,
        String articleType, String locale) {
        this(titleText, contentHTML, url, articleType, locale, null);
    }

    public RawArticle(String titleText, String contentHTML, String url,
            String articleType, String locale, String backendId) {
        this.titleText = titleText;
        this.contentHTML = contentHTML;
        this.url = url;
        this.articleType = articleType;
        this.locale = locale;
        this.backendId = backendId;
    }

    @JsonProperty("titleText")
    @Nullable
    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText;
    }

    @JsonProperty("contentHTML")
    @Nullable
    public String getContentHTML() {
        return contentHTML;
    }

    public void setContentHTML(String contentHTML) {
        this.contentHTML = contentHTML;
    }

    @JsonProperty("url")
    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("articleType")
    @NotNull
    public String getArticleType() {
        return articleType;
    }

    public void setArticleType(String articleType) {
        this.articleType = articleType;
    }

    /**
     * @return locale of this Article
     */
    @JsonProperty("locale")
    @NotNull
    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * @return backendId of translated content for this Article
     */
    @JsonProperty("backendId")
    @Nullable
    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(@Nullable String backendId) {
        this.backendId = backendId;
    }

    @Override
    public String toString() {
        return "RawArticle{" +
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
        if (!(o instanceof RawArticle)) return false;

        RawArticle rawArticle = (RawArticle) o;

        if (getTitleText() != null ?
            !getTitleText().equals(rawArticle.getTitleText()) :
            rawArticle.getTitleText() != null) return false;
        if (getContentHTML() != null ?
            !getContentHTML().equals(rawArticle.getContentHTML()) :
            rawArticle.getContentHTML() != null) return false;
        if (!getUrl().equals(rawArticle.getUrl()))
            return false;
        if (!getArticleType().equals(rawArticle.getArticleType()))
            return false;
        if (!getLocale().equals(rawArticle.getLocale()))
            return false;
        return getBackendId() != null ?
            getBackendId().equals(rawArticle.getBackendId()) :
            rawArticle.getBackendId() == null;

    }

    @Override
    public int hashCode() {
        int result = getTitleText() != null ? getTitleText().hashCode() : 0;
        result = 31 * result +
            (getContentHTML() != null ? getContentHTML().hashCode() : 0);
        result = 31 * result + getUrl().hashCode();
        result = 31 * result + getArticleType().hashCode();
        result =
            31 * result + getLocale().hashCode();
        result =
            31 * result +
                (getBackendId() != null ? getBackendId().hashCode() : 0);
        return result;
    }
}
