package org.zanata.magpie.model;

import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.model.type.LocaleCodeType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@TypeDef(name = "localeCode", typeClass = LocaleCodeType.class)
public class Locale extends ModelEntity {

    private static final long serialVersionUID = -7976081605962247643L;

    @Type(type = "localeCode")
    @NaturalId
    @NotNull
    private LocaleCode localeCode;

    @Size(max = 191)
    private String name;

    public Locale() {
    }

    public Locale(LocaleCode localeCode, String name) {
        this.localeCode = localeCode;
        this.name = name;
    }

    public LocaleCode getLocaleCode() {
        return localeCode;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof Locale)) return false;

        Locale locale = (Locale) o;

        return getLocaleCode() != null ?
                getLocaleCode().equals(locale.getLocaleCode()) :
                locale.getLocaleCode() == null;
    }

    @Override
    public int hashCode() {
        return getLocaleCode() != null ? getLocaleCode().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Locale{" +
                "localeCode=" + localeCode +
                ", name='" + name + '\'' +
                '}';
    }
}
