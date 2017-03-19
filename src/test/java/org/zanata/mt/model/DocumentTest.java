package org.zanata.mt.model;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentTest {

    @Test
    public void testEmptyConstructor() {
        Document doc = new Document();
    }

    @Test
    public void testConstructor() {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        Document doc = new Document("http://localhost",
                srcLocale, targetLocale);
        assertThat(doc.getUrl()).isEqualTo("http://localhost");
        assertThat(doc.getSrcLocale()).isEqualTo(srcLocale);
        assertThat(doc.getTargetLocale()).isEqualTo(targetLocale);
        assertThat(doc.getUrlHash()).isNotEmpty();
    }

    @Test
    public void testIncrementUsedCount() {
        Document doc = new Document();
        int useCount = doc.getUsedCount();
        assertThat(useCount).isEqualTo(0);
        doc.incrementUsedCount();
        int newUseCount = doc.getUsedCount();
        assertThat(newUseCount).isNotEqualTo(useCount).isEqualTo(useCount + 1);
    }

    @Test
    public void testEqualsAndHashcode() {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");

        Document doc1 = getDefaultDocument();
        Document doc2 = getDefaultDocument();
        assertThat(doc1.hashCode()).isEqualTo(doc2.hashCode());
        assertThat(doc1.equals(doc2)).isTrue();

        // change url
        doc2 = new Document("http://localhost2",
                srcLocale, targetLocale);
        assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode());
        assertThat(doc1.equals(doc2)).isFalse();

        // change srcLocale
        doc2 = new Document("http://localhost",
                new Locale(LocaleId.EN_US, "English"), targetLocale);
        assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode());
        assertThat(doc1.equals(doc2)).isFalse();

        // change targetLocale
        doc2 = new Document("http://localhost",
                srcLocale, new Locale(LocaleId.FR, "French"));
        assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode());
        assertThat(doc1.equals(doc2)).isFalse();

        // diff type
        String test = "test";
        assertThat(doc1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(doc1.equals(test)).isFalse();

    }

    private Document getDefaultDocument() {
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale targetLocale = new Locale(LocaleId.DE, "German");
        return new Document("http://localhost",
                srcLocale, targetLocale);
    }
}
