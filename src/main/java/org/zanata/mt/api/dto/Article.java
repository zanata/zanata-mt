package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * DTO for article to be translated
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class Article implements Serializable {
    private static final long serialVersionUID = 4123397809604573837L;

    private String url;

    private List<TypeString> contents;

    private String locale;

    private String backendId;

    @SuppressWarnings("unused")
    protected Article() {
    }

    public Article(List<TypeString> contents, String url, String locale) {
        this(contents, url, locale, null);
    }

    public Article(List<TypeString> contents, String url, String locale,
            String backendId) {
        this.contents = contents;
        this.url = url;
        this.locale = locale;
        this.backendId = backendId;
    }

    @JsonProperty("url")
    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("contents")
    @NotNull
    public List<TypeString> getContents() {
        return contents;
    }

    public void setContents(List<TypeString> contents) {
        this.contents = contents;
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
        return "Article{" +
                "url='" + url + '\'' +
                ", contents=" + contents +
                ", locale='" + locale + '\'' +
                ", backendId='" + backendId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;

        Article article = (Article) o;

        if (!url.equals(article.url))
            return false;
        if (!contents.equals(article.contents))
            return false;
        if (!locale.equals(article.locale))
            return false;
        return backendId != null ? backendId.equals(article.backendId) :
                article.backendId == null;

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + contents.hashCode();
        result = 31 * result + locale.hashCode();
        result = 31 * result + (backendId != null ? backendId.hashCode() : 0);
        return result;
    }
}
