package org.zanata.mt.model;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import java.util.HashSet;
import java.util.Set;

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
        Locale fromLang = new Locale(LocaleId.EN_US, "English US");
        TextFlow tf = new TextFlow(new Document(), "content", fromLang);
        assertThat(tf.getContent()).isEqualTo("content");
        assertThat(tf.getLocale()).isEqualTo(fromLang);
    }

    @Test
    public void testContent() {
        Locale fromLocale = new Locale(LocaleId.EN_US, "English US");
        TextFlow tf = new TextFlow(new Document(), "old content", fromLocale);
        String oldHash = tf.getContentHash();
        tf.setContent("content");
        assertThat(tf.getContent()).isEqualTo("content");
        assertThat(tf.getContentHash()).isNotEqualTo(oldHash);
    }

    @Test
    public void testDocuments() {
        Locale fromLocale = new Locale(LocaleId.EN_US, "English US");
        Locale toLocale = new Locale(LocaleId.DE, "German");

        Document doc = new Document("http://localhost",
                fromLocale, toLocale);

        TextFlow tf = new TextFlow(doc, "old content", fromLocale);
        assertThat(tf.getDocuments()).isNotEmpty().contains(doc);
    }

    @Test
    public void testEqualsAndHashcode() {
        Locale fromLocale = new Locale(LocaleId.EN_US, "English US");
        TextFlow tf1 = new TextFlow(new Document(),"content", fromLocale);
        TextFlow tf2 = new TextFlow(new Document(),"content", fromLocale);

        assertThat(tf1.hashCode()).isEqualTo(tf2.hashCode());
        assertThat(tf1.equals(tf2)).isTrue();

        // diff locale
        Locale newfromLocale = new Locale(LocaleId.EN, "English");
        tf2 = new TextFlow(new Document(), "content", newfromLocale);
        assertThat(tf1.hashCode()).isNotEqualTo(tf2);
        assertThat(tf1.equals(tf2)).isFalse();

        // diff content
        tf2 = new TextFlow(new Document(), "new content", fromLocale);
        assertThat(tf1.hashCode()).isNotEqualTo(tf2);
        assertThat(tf1.equals(tf2)).isFalse();

        // diff type
        String test = "test";
        assertThat(tf1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(tf1.equals(test)).isFalse();
    }

}
