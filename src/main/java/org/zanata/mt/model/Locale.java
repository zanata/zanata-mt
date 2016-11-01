package org.zanata.mt.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.constraints.NotEmpty;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.type.LocaleIdType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Access(AccessType.FIELD)
@TypeDef(name = "localeId", typeClass = LocaleIdType.class)
public class Locale extends ModelEntity {
    @NotEmpty
    @Size(max = 255)
    @Type(type = "localeId")
    @NaturalId
    private LocaleId localeId;

    @Size(max = 255)
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

    public void setLocaleId(LocaleId localeId) {
        this.localeId = localeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Locale)) return false;
        if (!super.equals(o)) return false;

        Locale locale = (Locale) o;

        if (localeId != null ? !localeId.equals(locale.localeId) :
            locale.localeId != null) return false;
        return name != null ? name.equals(locale.name) : locale.name == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (localeId != null ? localeId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
