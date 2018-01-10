package org.magpie.mt.model;

import org.junit.Test;
import org.magpie.mt.api.dto.LocaleCode;

import java.util.HashMap;
import java.util.Map;

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
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");
        Document doc = new Document("http://localhost/",
                srcLocale, targetLocale);
        assertThat(doc.getUrl()).isEqualTo("http://localhost");
        assertThat(doc.getFromLocale()).isEqualTo(srcLocale);
        assertThat(doc.getToLocale()).isEqualTo(targetLocale);
        assertThat(doc.getUrlHash()).isNotEmpty();
    }

    @Test
    public void testConstructor2() {
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");
        Map<String, TextFlow> textFlows = new HashMap<>();
        TextFlow tf = new TextFlow();
        tf.setContent("testing");
        textFlows.put(tf.getContentHash(), tf);
        Document doc = new Document("http://localhost",
                srcLocale, targetLocale, textFlows);

        assertThat(doc.getUrl()).isEqualTo("http://localhost");
        assertThat(doc.getFromLocale()).isEqualTo(srcLocale);
        assertThat(doc.getToLocale()).isEqualTo(targetLocale);
        assertThat(doc.getUrlHash()).isNotEmpty();
        assertThat(doc.getTextFlows()).isEqualTo(textFlows);
    }

    @Test
    public void testIncrementUsedCount() {
        Document doc = new Document();
        int useCount = doc.getCount();
        assertThat(useCount).isEqualTo(0);
        doc.incrementCount();
        int newUseCount = doc.getCount();
        assertThat(newUseCount).isNotEqualTo(useCount).isEqualTo(useCount + 1);
    }

    @Test
    public void testEqualsAndHashcode() {
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");

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
                new Locale(LocaleCode.EN_US, "English"), targetLocale);
        assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode());
        assertThat(doc1.equals(doc2)).isFalse();

        // change targetLocale
        doc2 = new Document("http://localhost",
                srcLocale, new Locale(LocaleCode.FR, "French"));
        assertThat(doc1.hashCode()).isNotEqualTo(doc2.hashCode());
        assertThat(doc1.equals(doc2)).isFalse();

        // diff type
        String test = "test";
        assertThat(doc1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(doc1.equals(test)).isFalse();

    }

    private Document getDefaultDocument() {
        Locale srcLocale = new Locale(LocaleCode.EN, "English");
        Locale targetLocale = new Locale(LocaleCode.DE, "German");
        return new Document("http://localhost",
                srcLocale, targetLocale);
    }
}
