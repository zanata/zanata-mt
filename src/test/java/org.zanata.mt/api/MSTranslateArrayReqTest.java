package org.zanata.mt.api;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.api.dto.Microsoft.MSString;
import org.zanata.mt.api.dto.Microsoft.MSTranslateArrayReq;
import org.zanata.mt.api.dto.Microsoft.Options;
import org.zanata.mt.util.DTOUtil;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReqTest {
    @Test
    public void testRoundTrip() throws JAXBException {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setSrcLanguage(LocaleId.EN_US);
        req.setTransLanguage(LocaleId.FR);
        req.getTexts().add(new MSString("test"));
        req.getTexts().add(new MSString("test1"));
        Options options = new Options();
        options.setContentType("xml");
        req.setOptions(options);
        String xml = DTOUtil.toXML(req);
        System.out.println(xml);
        MSTranslateArrayReq req1 = DTOUtil.toObject(xml, MSTranslateArrayReq.class);
        assertThat(req).isEqualTo(req1);
    }
}
