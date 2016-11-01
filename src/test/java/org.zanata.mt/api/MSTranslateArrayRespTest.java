package org.zanata.mt.api;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.zanata.mt.api.dto.Microsoft.MSString;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayResp;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayResponse;
import org.zanata.mt.api.dto.Microsoft.TextSentenceLength;
import org.zanata.mt.util.DTOUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayRespTest {
    @Test
    public void testRoundTrip() throws JAXBException {
        MSTranslateArrayResp resp = new MSTranslateArrayResp();
        MSTranslateArrayResponse re = new MSTranslateArrayResponse();
        re.setSrcLanguage("en-us");
        re.setOriginalTextSentenceLengths(new TextSentenceLength(1));
        re.setTranslatedTextSentenceLengths(new TextSentenceLength(2));
        re.getTranslatedText().add(new MSString("test21"));
        re.getTranslatedText().add(new MSString("test21"));
        resp.setResponse(re);

        String xml = DTOUtil.toXML(resp);
        MSTranslateArrayResp resp1 = DTOUtil.toObject(xml, MSTranslateArrayResp.class);
        assertThat(resp).isEqualTo(resp1);
    }
}
