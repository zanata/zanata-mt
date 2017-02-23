package org.zanata.mt.api.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Wrapper for LocaleId for language entity
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleId implements Serializable {
    private static final long serialVersionUID = 2193027706150405364L;

    public static final LocaleId EN = new LocaleId("en");
    public static final LocaleId EN_US = new LocaleId("en-us");
    public static final LocaleId DE = new LocaleId("de");
    public static final LocaleId FR = new LocaleId("fr");
    public static final LocaleId IT = new LocaleId("it");
    public static final LocaleId ES = new LocaleId("es");
    public static final LocaleId JA = new LocaleId("ja");
    public static final LocaleId KO = new LocaleId("ko");
    public static final LocaleId PT = new LocaleId("pt");
    public static final LocaleId RU = new LocaleId("ru");
    public static final LocaleId ZH_CN = new LocaleId("zh-cn");

    private String id;

    public LocaleId() {
        this("");
    }

    public LocaleId(@NotNull String localeId) {
        if (localeId.indexOf('_') != -1)
            throw new IllegalArgumentException(
                "expected lang[-country[-modifier]], got " + localeId);
        this.id = localeId.toLowerCase().intern();
    }

    public static LocaleId fromJavaName(String localeName) {
        return new LocaleId(localeName.replace('_', '-'));
    }

    @SuppressWarnings("null")
    public @NotNull String toJavaName() {
        return id.replace('-', '_');
    }

    public @NotNull String toString() {
        return id;
    }

    @Size(max = 255)
    @NotNull
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocaleId))
            return false;
        LocaleId localeId = (LocaleId) o;
        return id.equals(localeId.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
