package org.magpie.mt.api.dto;

import com.webcohesion.enunciate.metadata.DocumentationExample;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.magpie.mt.api.service.DocumentResource;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * JSON entity for a document translations request and response.
 * This entity is used for generic translations by accepting array of strings with type.
 *
 * Used in
 * {@link DocumentResource#translate(DocumentContent, LocaleCode)}
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class DocumentContent implements Serializable {
    private static final long serialVersionUID = 4123397809604573837L;

    private String url;

    private List<TypeString> contents;

    private String localeCode;

    private String backendId;

    private List<APIResponse> warnings;

    @SuppressWarnings("unused")
    protected DocumentContent() {
    }

    public DocumentContent(List<TypeString> contents, String url, String localeCode) {
        this(contents, url, localeCode, null, null);
    }

    public DocumentContent(List<TypeString> contents, String url, String localeCode,
            String backendId, List<APIResponse> warnings) {
        this.contents = contents;
        this.url = url;
        this.localeCode = localeCode;
        this.backendId = backendId;
        this.warnings = warnings;
    }

    /**
     * Source url of this document
     */
    @JsonProperty("url")
    @NotNull
    @DocumentationExample("http://example.com")
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
     * locale code of this DocumentContent
     */
    @JsonProperty("localeCode")
    @NotNull
    @Size(max = 128)
    @DocumentationExample("en-us")
    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    /**
     * backendId of translated content for this DocumentContent
     */
    @JsonProperty("backendId")
    @Nullable
    @Size(max = 20)
    @DocumentationExample("ms")
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
    @DocumentationExample("[]")
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
                ", localeCode='" + localeCode + '\'' +
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
        if (!localeCode.equals(docContent.localeCode))
            return false;
        return backendId != null ? backendId.equals(docContent.backendId) :
                docContent.backendId == null;

    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + contents.hashCode();
        result = 31 * result + localeCode.hashCode();
        result = 31 * result + (backendId != null ? backendId.hashCode() : 0);
        return result;
    }
}
