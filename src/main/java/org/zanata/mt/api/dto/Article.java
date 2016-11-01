package org.zanata.mt.api.dto;

import java.io.Serializable;

/**
 * DTO for KCS article
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class Article implements Serializable {
    private String title;

    private String divContent;

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
}
