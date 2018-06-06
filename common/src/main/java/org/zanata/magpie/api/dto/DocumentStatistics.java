package org.zanata.magpie.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.metadata.Label;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON entity for Document usedCount getStatistics.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
@Label("Document Statistics")
public class DocumentStatistics implements Serializable {

    private String url;

    private List<TranslationRequestStatistics> requestCounts = new ArrayList<>();

    @SuppressWarnings("unused")
    protected DocumentStatistics() {
    }

    public DocumentStatistics(String url) {
        this.url = url;
    }

    public void addRequestCount(String fromLocaleCode, String toLocaleCode,
            int count, int totalWordCount) {
        boolean inserted = false;
        for (TranslationRequestStatistics stats : requestCounts) {
            if (stats.getFromLocaleCode().equals(fromLocaleCode) &&
                    stats.getToLocaleCode().equals(toLocaleCode)) {
                stats.addCount(count);
                inserted = true;
                break;
            }
        }
        if (!inserted) {
            requestCounts.add(new TranslationRequestStatistics(fromLocaleCode,
                    toLocaleCode, count, totalWordCount));
        }
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

    /**
     * List of request count for each translation locale code
     */
    @JsonProperty("requestCounts")
    @NotNull
    public List<TranslationRequestStatistics> getRequestCounts() {
        return requestCounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentStatistics)) return false;

        DocumentStatistics documentStatistics = (DocumentStatistics) o;

        if (!getUrl().equals(documentStatistics.getUrl())) {
            return false;
        }
        return getRequestCounts().equals(documentStatistics.getRequestCounts());
    }

    @Override
    public int hashCode() {
        int result = getUrl().hashCode();
        result = 31 * result + getRequestCounts().hashCode();
        return result;
    }
}
