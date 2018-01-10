package org.magpie.mt.api.dto;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

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

        if (getLocaleCode() != null ?
                !getLocaleCode().equals(locale.getLocaleCode()) :
                locale.getLocaleCode() != null) return false;
        return getName() != null ? getName().equals(locale.getName()) :
                locale.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = getLocaleCode() != null ? getLocaleCode().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }
}
