package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Statistics entry for a single request with unique key
 * fromLocaleCode, toLocaleCode, count
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class TranslationRequestStatistics implements Serializable {
    private String fromLocaleCode;
    private String toLocaleCode;
    private int count;
    private int wordCount;

    @SuppressWarnings("unused")
    protected TranslationRequestStatistics() {
    }

    public TranslationRequestStatistics(String fromLocaleCode, String toLocaleCode,
            int count, int wordCount) {
        this.fromLocaleCode = fromLocaleCode;
        this.toLocaleCode = toLocaleCode;
        this.count = count;
        this.wordCount = wordCount;
    }

    public void addCount(int usedCount) {
        this.count += usedCount;
    }

    @JsonProperty("fromLocaleCode")
    @NotNull
    @Size(max = 128)
    public String getFromLocaleCode() {
        return fromLocaleCode;
    }

    @JsonProperty("toLocaleCode")
    @NotNull
    @Size(max = 128)
    public String getToLocaleCode() {
        return toLocaleCode;
    }

    @JsonProperty("count")
    public int getCount() {
        return count;
    }

    @JsonProperty("wordCount")
    public int getWordCount() {
        return wordCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TranslationRequestStatistics that = (TranslationRequestStatistics) o;

        if (count != that.count) return false;
        if (wordCount != that.wordCount) return false;
        if (fromLocaleCode != null ?
                !fromLocaleCode.equals(that.fromLocaleCode) :
                that.fromLocaleCode != null) return false;
        return toLocaleCode != null ? toLocaleCode.equals(that.toLocaleCode) :
                that.toLocaleCode == null;
    }

    @Override
    public int hashCode() {
        int result = fromLocaleCode != null ? fromLocaleCode.hashCode() : 0;
        result = 31 * result +
                (toLocaleCode != null ? toLocaleCode.hashCode() : 0);
        result = 31 * result + count;
        result = 31 * result + wordCount;
        return result;
    }
}
