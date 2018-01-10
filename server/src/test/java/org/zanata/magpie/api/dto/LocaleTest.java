package org.zanata.magpie.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleTest {

    @Test
    public void testEmptyConstructor() {
        Locale locale = new Locale();
    }

    @Test
    public void testConstructor() {
        Locale locale = new Locale("code", "name");
        assertThat(locale.getLocaleCode()).isEqualTo("code");
        assertThat(locale.getName()).isEqualTo("name");
    }

    @Test
    public void testLocaleCode() {
        Locale locale = new Locale();
        locale.setLocaleCode("code");
        assertThat(locale.getLocaleCode()).isEqualTo("code");
    }

    @Test
    public void testLocaleName() {
        Locale locale = new Locale();
        locale.setName("name");
        assertThat(locale.getName()).isEqualTo("name");
    }

    @Test
    public void testHashAndEquals() {
        Locale locale = new Locale("code", "name");
        Locale locale2 = new Locale("code", "name");

        assertThat(locale.equals(locale2)).isTrue();
        assertThat(locale.hashCode()).isEqualTo(locale2.hashCode());

        locale2.setLocaleCode("code1");

        assertThat(locale.equals(locale2)).isFalse();
        assertThat(locale.hashCode()).isNotEqualTo(locale2.hashCode());

        locale2.setName("name1");

        assertThat(locale.equals(locale2)).isFalse();
        assertThat(locale.hashCode()).isNotEqualTo(locale2.hashCode());
    }
}
