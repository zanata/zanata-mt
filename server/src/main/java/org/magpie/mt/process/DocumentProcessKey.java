package org.magpie.mt.process;

import org.magpie.mt.api.dto.LocaleCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Key used for handling document translation process.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentProcessKey implements Serializable {

    private final String url;
    private final LocaleCode fromLocaleCode;
    private final LocaleCode toLocaleCode;

    public DocumentProcessKey(@NotNull String url,
            @NotNull LocaleCode fromLocaleCode, @NotNull LocaleCode toLocaleCode) {
        this.url = url;
        this.fromLocaleCode = fromLocaleCode;
        this.toLocaleCode = toLocaleCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentProcessKey)) return false;

        DocumentProcessKey that = (DocumentProcessKey) o;

        if (!url.equals(that.url)) return false;
        if (!fromLocaleCode.equals(that.fromLocaleCode)) return false;
        return toLocaleCode.equals(that.toLocaleCode);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + fromLocaleCode.hashCode();
        result = 31 * result + toLocaleCode.hashCode();
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
