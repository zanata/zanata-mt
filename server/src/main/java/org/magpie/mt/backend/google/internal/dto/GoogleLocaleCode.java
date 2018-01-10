package org.magpie.mt.backend.google.internal.dto;

import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.backend.BackendLocaleCode;

import javax.validation.constraints.NotNull;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GoogleLocaleCode implements BackendLocaleCode {
    private String localeCode;

    public GoogleLocaleCode(@NotNull LocaleCode localeCode) {
        this(localeCode.getId());
    }

    public GoogleLocaleCode(@NotNull String localeCode) {
        this.localeCode = localeCode;
    }

    @Override
    public String getLocaleCode() {
        return localeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GoogleLocaleCode)) return false;

        GoogleLocaleCode that = (GoogleLocaleCode) o;

        return getLocaleCode() != null ?
                getLocaleCode().equals(that.getLocaleCode()) :
                that.getLocaleCode() == null;
    }

    @Override
    public int hashCode() {
        return getLocaleCode() != null ? getLocaleCode().hashCode() : 0;
    }
}
