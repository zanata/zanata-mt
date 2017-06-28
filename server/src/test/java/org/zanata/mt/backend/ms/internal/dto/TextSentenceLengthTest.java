package org.zanata.mt.backend.ms.internal.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextSentenceLengthTest {
    @Test
    public void testEmptyConstructor() {
        TextSentenceLength len = new TextSentenceLength();
    }

    @Test
    public void testConstructor() {
        TextSentenceLength len = new TextSentenceLength(100);
        assertThat(len.getValue()).isEqualTo(100);
    }

    @Test
    public void testValue() {
        TextSentenceLength len = new TextSentenceLength();
        len.setValue(200);
        assertThat(len.getValue()).isEqualTo(200);
    }

    @Test
    public void testEqualsAndHashcode() {
        TextSentenceLength len1 = new TextSentenceLength(100);
        TextSentenceLength len2 = new TextSentenceLength(200);

        assertThat(len1.hashCode()).isNotEqualTo(len2.hashCode());
        assertThat(len1.equals(len2)).isFalse();

        len2.setValue(null);
        assertThat(len1.hashCode()).isNotEqualTo(len2.hashCode());
        assertThat(len1.equals(len2)).isFalse();

        len2 = new TextSentenceLength(100);
        assertThat(len1.hashCode()).isEqualTo(len2.hashCode());
        assertThat(len1.equals(len2)).isTrue();
    }
}
