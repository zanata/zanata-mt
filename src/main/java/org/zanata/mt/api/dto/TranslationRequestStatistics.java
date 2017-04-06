package org.zanata.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Statistics entry for single a single request with unique key
 * fromLocaleCode, toLocaleCode, count
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class TranslationRequestStatistics implements Serializable {
    private String fromLocaleCode;
    private String toLocaleCode;
    private int count;

    @SuppressWarnings("unused")
    protected TranslationRequestStatistics() {
    }

    public TranslationRequestStatistics(String fromLocaleCode, String toLocaleCode,
            int count) {
        this.fromLocaleCode = fromLocaleCode;
        this.toLocaleCode = toLocaleCode;
        this.count = count;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationRequestStatistics)) return false;

        TranslationRequestStatistics
                that = (TranslationRequestStatistics) o;

        if (getCount() != that.getCount()) return false;
        if (getFromLocaleCode() != null ?
                !getFromLocaleCode().equals(that.getFromLocaleCode()) :
                that.getFromLocaleCode() != null) return false;
        return getToLocaleCode() != null ?
                getToLocaleCode().equals(that.getToLocaleCode()) :
                that.getToLocaleCode() == null;
    }

    @Override
    public int hashCode() {
        int result =
                getFromLocaleCode() != null ?
                        getFromLocaleCode().hashCode() : 0;
        result = 31 * result +
                (getToLocaleCode() != null ? getToLocaleCode().hashCode() :
                        0);
        result = 31 * result + getCount();
        return result;
    }
}
