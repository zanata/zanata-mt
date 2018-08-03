package org.zanata.magpie.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleCodeTest {

    @Test
    public void testEmptyConstructor() {
        LocaleCode _ignored = new LocaleCode();
    }

    @Test
    public void testConstructor() {
        LocaleCode localeCode = new LocaleCode("en");
        assertThat(localeCode.getId()).isEqualTo("en");
    }

    @Test
    public void testConstructorIllegal() {
        assertThatThrownBy(() -> new LocaleCode("en_US"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetLanguage() {
        assertThat(new LocaleCode("en-US").getLanguage()).isEqualTo("en");
        assertThat(new LocaleCode("en").getLanguage()).isEqualTo("en");
        assertThat(new LocaleCode("zh-CN").getLanguage()).isEqualTo("zh-hans");
        assertThat(new LocaleCode("zh-SG").getLanguage()).isEqualTo("zh-hans");
        assertThat(new LocaleCode("zh-Hans-CN").getLanguage()).isEqualTo("zh-hans");
        assertThat(new LocaleCode("zh-Hans").getLanguage()).isEqualTo("zh-hans");
        assertThat(new LocaleCode("zh-HK").getLanguage()).isEqualTo("zh-hant");
        assertThat(new LocaleCode("zh-MO").getLanguage()).isEqualTo("zh-hant");
        assertThat(new LocaleCode("zh-TW").getLanguage()).isEqualTo("zh-hant");
        assertThat(new LocaleCode("zh-Hant").getLanguage()).isEqualTo("zh-hant");
        assertThat(new LocaleCode("zh-Hant-TW").getLanguage()).isEqualTo("zh-hant");
        assertThat(new LocaleCode("zh-hant-TW").getLanguage()).isEqualTo("zh-hant");
        assertThat(new LocaleCode("zh-hant-tw").getLanguage()).isEqualTo("zh-hant");
    }

    @Test
    public void testToJavaName() {
        LocaleCode localeCode = new LocaleCode("en-US");
        assertThat(localeCode.toJavaName()).isEqualTo("en_us");
    }

    @Test
    public void testFromJavaName() {
        LocaleCode localeCode = LocaleCode.fromJavaName("en_US");
        LocaleCode expected = new LocaleCode("en-us");
        assertThat(localeCode).isEqualTo(expected);
    }

    @Test
    public void testEqualsAndHashcode() {
        LocaleCode localeCode1 = new LocaleCode("en");
        LocaleCode localeCode2 = new LocaleCode("en");

        assertThat(localeCode1.equals(localeCode2)).isTrue();
        assertThat(localeCode1.hashCode()).isEqualTo(localeCode2.hashCode());
    }
}
