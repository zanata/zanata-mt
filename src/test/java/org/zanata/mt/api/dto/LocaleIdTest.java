package org.zanata.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleIdTest {

    @Test
    public void testEmptyConstructor() {
        LocaleId localeId = new LocaleId();
    }

    @Test
    public void testConstructor() {
        LocaleId localeId = new LocaleId("en");
        assertThat(localeId.getId()).isEqualTo("en");
    }

    @Test
    public void testConstructorIllegal() {
        assertThatThrownBy(() -> new LocaleId("en_US"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testToJavaName() {
        LocaleId localeId = new LocaleId("en-US");
        assertThat(localeId.toJavaName()).isEqualTo("en_us");
    }

    @Test
    public void testFromJavaName() {
        LocaleId localeId = new LocaleId("en-us");
        assertThat(LocaleId.fromJavaName("en_US")).isEqualTo(localeId);
    }

    @Test
    public void testEqualsAndHashcode() {
        LocaleId localeId1 = new LocaleId("en");
        LocaleId localeId2 = new LocaleId("en");

        assertThat(localeId1.equals(localeId2)).isTrue();
        assertThat(localeId1.hashCode()).isEqualTo(localeId2.hashCode());
    }
}
