package org.zanata.mt.process;

import org.zanata.mt.api.dto.LocaleId;

import java.io.Serializable;

/**
 * Key used for handling document translation process.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentProcessKey implements Serializable {

    private final String url;
    private final LocaleId fromLocaleCode;
    private final LocaleId toLocaleCode;

    public DocumentProcessKey(String url,
            LocaleId fromLocaleCode, LocaleId toLocaleCode) {
        this.url = url;
        this.fromLocaleCode = fromLocaleCode;
        this.toLocaleCode = toLocaleCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentProcessKey)) return false;

        DocumentProcessKey that = (DocumentProcessKey) o;

        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;
        if (fromLocaleCode != null ? !fromLocaleCode.equals(that.fromLocaleCode) :
                that.fromLocaleCode != null) return false;
        return toLocaleCode != null ?
                toLocaleCode.equals(that.toLocaleCode) :
                that.toLocaleCode == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result +
                (fromLocaleCode != null ? fromLocaleCode.hashCode() : 0);
        result =
                31 * result +
                        (toLocaleCode != null ? toLocaleCode.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DocumentProcessKey{" +
                "url='" + url + '\'' +
                ", fromLocaleCode=" + fromLocaleCode +
                ", toLocaleCode=" + toLocaleCode +
                '}';
    }
}
