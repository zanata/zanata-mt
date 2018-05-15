package org.zanata.magpie.backend.ms.internal.dto;

import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;

import javax.validation.constraints.NotNull;

/**
 * A wrapper for locale code used in MS translator
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSLocaleCode implements BackendLocaleCode {
    private String localeCode;

    public MSLocaleCode(@NotNull LocaleCode localeCode) {
        this(localeCode.getId());
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
