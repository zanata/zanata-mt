package org.zanata.mt.api.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * DTO for content to be translated
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class Article implements Serializable {
    private static final long serialVersionUID = 4123397809604573837L;

    private String title;

    private String content;

    @NotNull
    private String url;

    @SuppressWarnings("unused")
    protected Article() {
    }

    public Article(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
            ", content='" + content + '\'' +
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
        if (content != null ? !content.equals(article.content) :
            article.content != null) return false;
        return url != null ? url.equals(article.url) : article.url == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }
}
