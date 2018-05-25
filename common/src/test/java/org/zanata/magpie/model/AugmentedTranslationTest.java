package org.zanata.magpie.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AugmentedTranslationTest {

    @Test
    public void testConstructor() {
        AugmentedTranslation translation = getDefault();
        assertThat(translation.getPlainTranslation()).isEqualTo("plain");
        assertThat(translation.getRawTranslation()).isEqualTo("raw");
    }

    @Test
    public void testPlainTranslation() {
        AugmentedTranslation translation = new AugmentedTranslation(null, null);
        translation.setPlainTranslation("plain");
        assertThat(translation.getPlainTranslation()).isEqualTo("plain");
    }

    @Test
    public void testRawTranslation() {
        AugmentedTranslation translation = new AugmentedTranslation(null, null);
        translation.setRawTranslation("raw");
        assertThat(translation.getRawTranslation()).isEqualTo("raw");
    }

    @Test
    public void testEqualsAndHashcode() {
        AugmentedTranslation translation = getDefault();
        AugmentedTranslation translation2 = getDefault();

        assertThat(translation.hashCode()).isEqualTo(translation2.hashCode());
        assertThat(translation.equals(translation2)).isTrue();

        // change translation
        translation2 = getDefault();
        translation2.setPlainTranslation("plain2");
        assertThat(translation.hashCode()).isNotEqualTo(translation2.hashCode());
        assertThat(translation.equals(translation2)).isFalse();

        // change null translation
        translation2 = getDefault();
        translation2.setPlainTranslation(null);
        assertThat(translation.hashCode()).isNotEqualTo(translation2.hashCode());
        assertThat(translation.equals(translation2)).isFalse();

        // change raw
        translation2 = getDefault();
        translation2.setRawTranslation("raw2");
        assertThat(translation.hashCode()).isNotEqualTo(translation2.hashCode());
        assertThat(translation.equals(translation2)).isFalse();

        // change null raw
        translation2 = getDefault();
        translation2.setRawTranslation(null);
        assertThat(translation.hashCode()).isNotEqualTo(translation2.hashCode());
        assertThat(translation.equals(translation2)).isFalse();

        // diff type
        String test = "test";
        assertThat(translation.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(translation.equals(test)).isFalse();
    }

    private AugmentedTranslation getDefault() {
        return new AugmentedTranslation("plain", "raw");
    }
}
