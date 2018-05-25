package org.zanata.magpie.backend.ms.internal.dto;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.zanata.magpie.util.DTOUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayRespTest {
    private DTOUtil dtoUtil = new DTOUtil();
    @Test
    public void testRoundTrip() throws JAXBException {
        MSTranslateArrayResp resp = getDefaultResp();

        String xml = dtoUtil.toXML(resp);
        MSTranslateArrayResp resp1 = dtoUtil
            .toObject(xml, MSTranslateArrayResp.class);
        assertThat(resp).isEqualTo(resp1);
    }

    @Test
    public void testEqualsAndHashCode() {
        MSTranslateArrayResp resp1 = getDefaultResp();
        MSTranslateArrayResp resp2 = new MSTranslateArrayResp();
        assertThat(resp1.hashCode()).isNotEqualTo(resp2.hashCode());
        assertThat(resp1.equals(resp2)).isFalse();

        resp2 = new MSTranslateArrayResp();
        resp2.setResponse(null);
        assertThat(resp1.hashCode()).isNotEqualTo(resp2.hashCode());
        assertThat(resp1.equals(resp2)).isFalse();

        resp2 = getDefaultResp();
        assertThat(resp1.hashCode()).isEqualTo(resp2.hashCode());
        assertThat(resp1.equals(resp2)).isTrue();

    }

    private MSTranslateArrayResp getDefaultResp() {
        MSTranslateArrayResp resp = new MSTranslateArrayResp();

        MSTranslateArrayResponse re1 = new MSTranslateArrayResponse();
        re1.setSrcLanguage("en-us");
        re1.setAlignment("alignment");
        re1.setOriginalTextSentenceLengths(new TextSentenceLength(1));
        re1.setTranslatedTextSentenceLengths(new TextSentenceLength(2));
        re1.setTranslatedText(new MSString("test1"));

        MSTranslateArrayResponse re2 = new MSTranslateArrayResponse();
        re2.setSrcLanguage("en-us");
        re2.setAlignment("alignment2");
        re2.setOriginalTextSentenceLengths(new TextSentenceLength(1));
        re2.setTranslatedTextSentenceLengths(new TextSentenceLength(2));
        re2.setTranslatedText(new MSString("test2"));

        resp.getResponse().add(re1);
        resp.getResponse().add(re2);
        return resp;
    }
}
