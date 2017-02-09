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
        return "Article2{" +
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

        if (url != null ? !url.equals(article.url) : article.url != null)
            return false;
        if (contents != null ? !contents.equals(article.contents) :
                article.contents != null) return false;
        if (locale != null ? !locale.equals(article.locale) :
                article.locale != null) return false;
        return backendId != null ? backendId.equals(article.backendId) :
                article.backendId == null;

    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (contents != null ? contents.hashCode() : 0);
        result = 31 * result + (locale != null ? locale.hashCode() : 0);
        result = 31 * result + (backendId != null ? backendId.hashCode() : 0);
        return result;
    }
}
