package org.zanata.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TypeStringTest {

    @Test
    public void testEmptyConstructor() {
        TypeString typeString = new TypeString();
    }

    @Test
    public void testConstructor() {
        TypeString typeString = new TypeString("value", "type", "meta");
        assertThat(typeString.getType()).isEqualTo("type");
        assertThat(typeString.getValue()).isEqualTo("value");
    }

    @Test
    public void testValue() {
        TypeString typeString = new TypeString();
        typeString.setValue("value");
        assertThat(typeString.getValue()).isEqualTo("value");
    }

    @Test
    public void testType() {
        TypeString typeString = new TypeString();
        typeString.setType("type");
        assertThat(typeString.getType()).isEqualTo("type");
    }

    @Test
    public void testMetadata() {
        TypeString typeString = new TypeString();
        typeString.setMetadata("meta");
        assertThat(typeString.getMetadata()).isEqualTo("meta");
    }

    @Test
    public void testEqualsAndHashcode() {
        TypeString typeString1 = getDefault();

        // change value and type
        TypeString typeString2 = getDefault();
        typeString2.setValue("value2");
        typeString2.setType("type2");

        assertThat(typeString1.equals(typeString2)).isFalse();
        assertThat(typeString1.hashCode()).isNotEqualTo(typeString2.hashCode());

        // change type
        typeString2 = getDefault();
        typeString2.setType("type2");

        assertThat(typeString1.equals(typeString2)).isFalse();
        assertThat(typeString1.hashCode()).isNotEqualTo(typeString2.hashCode());

        // change meta
        typeString2 = getDefault();
        typeString2.setMetadata("meta2");

        assertThat(typeString1.equals(typeString2)).isFalse();
        assertThat(typeString1.hashCode()).isNotEqualTo(typeString2.hashCode());

        typeString2 = getDefault();

        assertThat(typeString1.equals(typeString2)).isTrue();
        assertThat(typeString1.hashCode()).isEqualTo(typeString2.hashCode());
    }

    private TypeString getDefault() {
        return new TypeString("value", "type", "meta");
    }
}
