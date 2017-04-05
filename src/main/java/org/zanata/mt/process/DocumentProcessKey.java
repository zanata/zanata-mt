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
    private final LocaleId srcLocaleId;
    private final LocaleId transLocaleId;

    public DocumentProcessKey(String url,
            LocaleId srcLocaleId, LocaleId transLocaleId) {
        this.url = url;
        this.srcLocaleId = srcLocaleId;
        this.transLocaleId = transLocaleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentProcessKey)) return false;

        DocumentProcessKey that = (DocumentProcessKey) o;

        if (url != null ? !url.equals(that.url) : that.url != null)
            return false;
        if (srcLocaleId != null ? !srcLocaleId.equals(that.srcLocaleId) :
                that.srcLocaleId != null) return false;
        return transLocaleId != null ?
                transLocaleId.equals(that.transLocaleId) :
                that.transLocaleId == null;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result +
                (srcLocaleId != null ? srcLocaleId.hashCode() : 0);
        result =
                31 * result +
                        (transLocaleId != null ? transLocaleId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DocumentProcessKey{" +
                "url='" + url + '\'' +
                ", srcLocaleId=" + srcLocaleId +
                ", transLocaleId=" + transLocaleId +
                '}';
    }
}
