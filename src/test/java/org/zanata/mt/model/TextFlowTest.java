package org.zanata.mt.model;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TextFlowTest {

    @Test
    public void testEmptyConstructor() {
        TextFlow tf = new TextFlow();
        assertThat(tf.getContent()).isNull();
        assertThat(tf.getTargets()).isNotNull();
    }

    @Test
    public void testConstructor() {
        Locale srcLang = new Locale(LocaleId.EN_US, "English US");
        TextFlow tf = new TextFlow("content", srcLang);
        assertThat(tf.getContent()).isEqualTo("content");
        assertThat(tf.getLocale()).isEqualTo(srcLang);
    }

    @Test
    public void testContent() {
        Locale srcLang = new Locale(LocaleId.EN_US, "English US");
        TextFlow tf = new TextFlow("old content", srcLang);
        String oldHash = tf.getHash();
        tf.setContent("content");
        assertThat(tf.getContent()).isEqualTo("content");
        assertThat(tf.getHash()).isNotEqualTo(oldHash);
    }

    @Test
    public void testEqualsAndHashcode() {
        Locale srcLang = new Locale(LocaleId.EN_US, "English US");
        TextFlow tf1 = new TextFlow("content", srcLang);
        TextFlow tf2 = new TextFlow("content", srcLang);

        assertThat(tf1.hashCode()).isEqualTo(tf2.hashCode());
        assertThat(tf1.equals(tf2)).isTrue();

        // diff locale
        Locale newSrcLang = new Locale(LocaleId.EN, "English");
        tf2 = new TextFlow("content", newSrcLang);
        assertThat(tf1.hashCode()).isNotEqualTo(tf2);
        assertThat(tf1.equals(tf2)).isFalse();

        // diff content
        tf2 = new TextFlow("new content", srcLang);
        assertThat(tf1.hashCode()).isNotEqualTo(tf2);
        assertThat(tf1.equals(tf2)).isFalse();

        // diff type
        String test = "test";
        assertThat(tf1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(tf1.equals(test)).isFalse();
    }

}
