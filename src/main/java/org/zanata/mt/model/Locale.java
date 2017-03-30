package org.zanata.mt.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.type.LocaleIdType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class Locale extends ModelEntity {

    private static final long serialVersionUID = -7976081605962247643L;

    @Type(type = "localeId")
    @NaturalId
    @NotNull
    @Column(unique = true)
    private LocaleId localeId;

    @Size(max = 191)
    private String name;

    public Locale() {
    }

    public Locale(LocaleId localeId, String name) {
        this.localeId = localeId;
        this.name = name;
    }

    public LocaleId getLocaleId() {
        return localeId;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Locale)) return false;

        Locale locale = (Locale) o;

        return getLocaleId() != null ?
                getLocaleId().equals(locale.getLocaleId()) :
                locale.getLocaleId() == null;
    }

    @Override
    public int hashCode() {
        return getLocaleId() != null ? getLocaleId().hashCode() : 0;
    }
}
