package org.zanata.magpie.api.dto;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Wrapper for LocaleCode for language entity
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleCode implements Serializable {
    private static final long serialVersionUID = 2193027706150405364L;

    public static final LocaleCode DE = new LocaleCode("de");
    public static final LocaleCode EN = new LocaleCode("en");
    public static final LocaleCode EN_US = new LocaleCode("en-us");
    public static final LocaleCode ES = new LocaleCode("es");
    public static final LocaleCode FR = new LocaleCode("fr");
    public static final LocaleCode IT = new LocaleCode("it");
    public static final LocaleCode JA = new LocaleCode("ja");
    public static final LocaleCode KO = new LocaleCode("ko");
    public static final LocaleCode NL = new LocaleCode("nl");
    public static final LocaleCode PL = new LocaleCode("pl");
    public static final LocaleCode PT = new LocaleCode("pt");
    public static final LocaleCode RU = new LocaleCode("ru");
    public static final LocaleCode ZH_HANS = new LocaleCode("zh-hans");
    public static final LocaleCode ZH_HANT = new LocaleCode("zh-hant");

    private String id;

    public LocaleCode(@NotNull String localeCode) {
        if (localeCode.indexOf('_') != -1)
            throw new IllegalArgumentException(
                "expected lang[-country[-modifier]], got " + localeCode);
        this.id = localeCode.toLowerCase().intern();
    }

    @SuppressWarnings("unused")
    protected LocaleCode() {
        this("");
    }

    public static LocaleCode fromJavaName(String localeName) {
        return new LocaleCode(localeName.replace('_', '-'));
    }

    @SuppressWarnings("null")
    public @NotNull String toJavaName() {
        return id.replace('-', '_');
    }

    public @NotNull String toString() {
        return id;
    }

    @Size(max = 128)
    @NotNull
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LocaleCode))
            return false;
        LocaleCode localeCode = (LocaleCode) o;
        return id.equals(localeCode.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
