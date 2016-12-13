package org.zanata.mt.api.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * DTO for KCS article
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class Article implements Serializable {
    private String title;

    private String divContent;

    @NotNull
    private String url;

    @SuppressWarnings("unused")
    protected Article() {
    }

    public Article(String title, String divContent, String url) {
        this.title = title;
        this.divContent = divContent;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDivContent() {
        return divContent;
    }

    public void setDivContent(String divContent) {
        this.divContent = divContent;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Article{" +
            "title='" + title + '\'' +
            ", divContent='" + divContent + '\'' +
            ", url='" + url + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article)) return false;

        Article article = (Article) o;

        if (title != null ? !title.equals(article.title) :
            article.title != null)
            return false;
        if (divContent != null ? !divContent.equals(article.divContent) :
            article.divContent != null) return false;
        return url != null ? url.equals(article.url) : article.url == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (divContent != null ? divContent.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
