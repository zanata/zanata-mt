package org.zanata.mt.backend.ms.internal.dto;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResponse;
import org.zanata.mt.backend.ms.internal.dto.TextSentenceLength;
import org.zanata.mt.util.DTOUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayRespTest {
    @Test
    public void testRoundTrip() throws JAXBException {
        MSTranslateArrayResp resp = new MSTranslateArrayResp();

        MSTranslateArrayResponse re1 = new MSTranslateArrayResponse();
        re1.setSrcLanguage("en-us");
        re1.setOriginalTextSentenceLengths(new TextSentenceLength(1));
        re1.setTranslatedTextSentenceLengths(new TextSentenceLength(2));
        re1.setTranslatedText(new MSString("test1"));

        MSTranslateArrayResponse re2 = new MSTranslateArrayResponse();
        re2.setSrcLanguage("en-us");
        re2.setOriginalTextSentenceLengths(new TextSentenceLength(1));
        re2.setTranslatedTextSentenceLengths(new TextSentenceLength(2));
        re2.setTranslatedText(new MSString("test2"));

        resp.getResponse().add(re1);
        resp.getResponse().add(re2);

        String xml = DTOUtil.toXML(resp);
        MSTranslateArrayResp resp1 = DTOUtil
            .toObject(xml, MSTranslateArrayResp.class);
        assertThat(resp).isEqualTo(resp1);
    }
}
