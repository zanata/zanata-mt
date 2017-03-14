package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.mt.api.service.DocumentContentTranslatorResource;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * JSON entity for a document translations request and response.
 * This entity is used for generic translations by accepting array of strings with type.
 *
 * Used in
 * {@link DocumentContentTranslatorResource#translate(DocumentContent, LocaleId)}
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class DocumentContent implements Serializable {
    private static final long serialVersionUID = 4123397809604573837L;

    private String url;

    private List<TypeString> contents;

    private String locale;

    private String backendId;

    private List<APIResponse> warnings;

    @SuppressWarnings("unused")
    protected DocumentContent() {
    }

    public DocumentContent(List<TypeString> contents, String url, String locale) {
        this(contents, url, locale, null, null);
    }

    public DocumentContent(List<TypeString> contents, String url, String locale,
            String backendId, List<APIResponse> warnings) {
        this.contents = contents;
        this.url = url;
        this.locale = locale;
        this.backendId = backendId;
        this.warnings = warnings;
    }

    /**
     * Source url of this document
     */
    @JsonProperty("url")
    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Content of this document in array of {@link TypeString}
     */
    @JsonProperty("contents")
    @NotNull
    public List<TypeString> getContents() {
        return contents;
    }

    public void setContents(List<TypeString> contents) {
        this.contents = contents;
    }

    /**
     * locale of this DocumentContent
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
     * backendId of translated content for this DocumentContent
     */
    @JsonProperty("backendId")
    @Nullable
    public String getBackendId() {
        return backendId;
    }

    public void setBackendId(@Nullable String backendId) {
        this.backendId = backendId;
    }

    /**
     * warning messages {@link APIResponse}
     */
    @JsonProperty("warnings")
    @Nullable
    public List<APIResponse> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<APIResponse> warnings) {
        this.warnings = warnings;
    }

    @Override
    public String toString() {
        return "DocumentContent{" +
                "url='" + url + '\'' +
                ", contents=" + contents +
                ", locale='" + locale + '\'' +
                ", backendId='" + backendId + '\'' +
                ", warnings=" + warnings +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentContent)) return false;

        DocumentContent docContent = (DocumentContent) o;

        if (!url.equals(docContent.url))
            return false;
        if (!contents.equals(docContent.contents))
            return false;
        if (!locale.equals(docContent.locale))
            return false;
        return backendId != null ? backendId.equals(docContent.backendId) :
                docContent.backendId == null;

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
