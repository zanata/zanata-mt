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
        LocaleCode localeCode = new LocaleCode();
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
    public void testToJavaName() {
        LocaleCode localeCode = new LocaleCode("en-US");
        assertThat(localeCode.toJavaName()).isEqualTo("en_us");
    }

    @Test
    public void testFromJavaName() {
        LocaleCode localeCode = new LocaleCode("en-us");
        assertThat(LocaleCode.fromJavaName("en_US")).isEqualTo(localeCode);
    }

    @Test
    public void testEqualsAndHashcode() {
        LocaleCode localeCode1 = new LocaleCode("en");
        LocaleCode localeCode2 = new LocaleCode("en");

        assertThat(localeCode1.equals(localeCode2)).isTrue();
        assertThat(localeCode1.hashCode()).isEqualTo(localeCode2.hashCode());
    }
}
