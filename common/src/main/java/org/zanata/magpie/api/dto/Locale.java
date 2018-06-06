package org.zanata.magpie.api.dto;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonSerialize
public class Locale implements Serializable {

    private String localeCode;

    private String name;

    @SuppressWarnings("unused")
    public Locale() {
    }

    public Locale(String localeCode, String name) {
        this.localeCode = localeCode;
        this.name = name;
    }

    /**
     * @return Locale code
     */
    @JsonProperty("localeCode")
    @NotNull
    @Size(max = 128)
    public String getLocaleCode() {
        return localeCode;
    }

    public void setLocaleCode(String localeCode) {
        this.localeCode = localeCode;
    }

    /**
     * @return Name of the locale
     */
    @JsonProperty("name")
    @NotNull
    @Size(max = 191)
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

        Locale locale = (Locale) o;

        if (!getLocaleCode().equals(locale.getLocaleCode())) {
            return false;
        }
        return getName().equals(locale.getName());
    }

    @Override
    public int hashCode() {
        int result = getLocaleCode().hashCode();
        result = 31 * result + getName().hashCode();
        return result;
    }
}
