package org.zanata.mt.backend.ms.internal.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayResponseTest {

    @Test
    public void testConstructor() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
    }

    @Test
    public void testSrcLanguage() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
        res.setSrcLanguage("en");
        assertThat(res.getSrcLanguage()).isEqualTo("en");
    }

    @Test
    public void testAlignment() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
        res.setAlignment("alignment");
        assertThat(res.getAlignment()).isEqualTo("alignment");
    }

    @Test
    public void testOriginalTextSentenceLengths() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
        TextSentenceLength len = new TextSentenceLength(10);
        res.setOriginalTextSentenceLengths(len);
        assertThat(res.getOriginalTextSentenceLengths()).isEqualTo(len);
    }

    @Test
    public void testTranslatedTextSentenceLengths() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
        TextSentenceLength len = new TextSentenceLength(10);
        res.setTranslatedTextSentenceLengths(len);
        assertThat(res.getTranslatedTextSentenceLengths()).isEqualTo(len);
    }

    @Test
    public void testTranslatedText() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
        MSString text = new MSString("value");
        res.setTranslatedText(text);
        assertThat(res.getTranslatedText()).isEqualTo(text);
    }

    @Test
    public void testEqualsAndHashCode() {
        MSTranslateArrayResponse res = getDefaultResp();
        MSTranslateArrayResponse res2 = getDefaultResp();

        assertThat(res.hashCode()).isEqualTo(res2.hashCode());
        assertThat(res.equals(res2)).isTrue();


        // change src lang
        res2 = getDefaultResp();
        res2.setSrcLanguage("en-us");
        assertThat(res.hashCode()).isNotEqualTo(res2);
        assertThat(res.equals(res2)).isFalse();

        // change alignment
        res2 = getDefaultResp();
        res2.setAlignment("alignment2");
        assertThat(res.hashCode()).isNotEqualTo(res2);
        assertThat(res.equals(res2)).isFalse();

        // change OriginalTextSentenceLengths
        res2 = getDefaultResp();
        res2.setOriginalTextSentenceLengths(null);
        assertThat(res.hashCode()).isNotEqualTo(res2);
        assertThat(res.equals(res2)).isFalse();

        // change TranslatedTextSentenceLengths
        res2 = getDefaultResp();
        res2.setTranslatedTextSentenceLengths(null);
        assertThat(res.hashCode()).isNotEqualTo(res2);
        assertThat(res.equals(res2)).isFalse();

        // change TranslatedText
        res2 = getDefaultResp();
        res2.setTranslatedText(null);
        assertThat(res.hashCode()).isNotEqualTo(res2);
        assertThat(res.equals(res2)).isFalse();
    }

    private MSTranslateArrayResponse getDefaultResp() {
        MSTranslateArrayResponse res = new MSTranslateArrayResponse();
        res.setSrcLanguage("en");
        res.setAlignment("alignment");
        TextSentenceLength len = new TextSentenceLength(10);
        TextSentenceLength len2 = new TextSentenceLength(100);
        res.setOriginalTextSentenceLengths(len);
        res.setTranslatedTextSentenceLengths(len2);
        res.setTranslatedText(new MSString("value"));

        return res;
    }
}
