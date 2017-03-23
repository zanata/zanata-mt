package org.zanata.mt.backend.ms.internal.dto;

import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.backend.BackendLocaleCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A wrapper for locale code used in MS translator
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSLocaleCode implements BackendLocaleCode {
    private String localeCode;

    public MSLocaleCode(@NotNull LocaleId localeId) {
        this(localeId.getId());
    }

    public MSLocaleCode(@NotNull String localeCode) {
        this.localeCode = localeCode;
    }

    @Override
    public String getLocaleCode() {
        return localeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MSLocaleCode)) return false;

        MSLocaleCode that = (MSLocaleCode) o;

        return getLocaleCode() != null ?
                getLocaleCode().equals(that.getLocaleCode()) :
                that.getLocaleCode() == null;
    }

    @Override
    public int hashCode() {
        return getLocaleCode() != null ? getLocaleCode().hashCode() : 0;
    }
}
