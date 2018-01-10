package org.magpie.mt.backend.ms.internal.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSStringTest {

    @Test
    public void testEmptyConstructor() {
        MSString msString = new MSString();
    }

    @Test
    public void testConstructor() {
        MSString msString = new MSString("value");
        assertThat(msString.getValue()).isEqualTo("value");
    }

    @Test
    public void testValue() {
        MSString msString = new MSString();
        msString.setValue("value");
        assertThat(msString.getValue()).isEqualTo("value");
    }

    @Test
    public void testEqualsAndHashcode() {
        MSString msString1 = new MSString("value1");
        MSString msString2 = new MSString("value2");
        assertThat(msString1.hashCode()).isNotEqualTo(msString2.hashCode());
        assertThat(msString1.equals(msString2)).isFalse();

        msString2.setValue(null);
        assertThat(msString1.hashCode()).isNotEqualTo(msString2.hashCode());
        assertThat(msString1.equals(msString2)).isFalse();

        msString2 = new MSString("value1");
        assertThat(msString1.hashCode()).isEqualTo(msString2.hashCode());
        assertThat(msString1.equals(msString2)).isTrue();
    }
}
