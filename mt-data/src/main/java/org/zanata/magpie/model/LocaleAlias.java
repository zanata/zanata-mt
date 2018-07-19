package org.zanata.magpie.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.zanata.magpie.api.dto.LocaleCode;

@Entity
@Access(AccessType.FIELD)
public class LocaleAlias extends ModelEntity {
    private static final long serialVersionUID = 1L;

    @Type(type = "localeCode")
    @NaturalId
    @NotNull
    private LocaleCode localeCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "localeId", nullable = false, updatable = false)
    @NotNull
    private Locale locale;

    public LocaleAlias() {
    }

    public LocaleAlias(LocaleCode localeCode, Locale locale) {
        this.localeCode = localeCode;
        this.locale = locale;
    }

    public Locale getLocale() {
        return locale;
    }

    public LocaleCode getLocaleCode() {
        return localeCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocaleAlias other = (LocaleAlias) o;

        if (locale != null ? !locale.equals(other.locale) :
                other.locale != null) return false;
        return localeCode != null ? localeCode.equals(other.localeCode) :
                other.localeCode == null;
    }

    @Override
    public int hashCode() {
        int result = locale != null ? locale.hashCode() : 0;
        result = 31 * result + (localeCode != null ? localeCode.hashCode() : 0);
        return result;
    }
}
