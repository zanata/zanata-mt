package org.zanata.mt.backend.ms.internal.dto;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReq;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayReqOptions;
import org.zanata.mt.model.Locale;
import org.zanata.mt.util.DTOUtil;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReqTest {

    @Test
    public void testEmptyConstructor() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
    }

    @Test
    public void testAppId() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setAppId("appId");
        assertThat(req.getAppId()).isEqualTo("appId");
    }

    @Test
    public void testSrcLanguage() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setSrcLanguage(LocaleId.DE);
        assertThat(req.getSrcLanguage()).isEqualTo(LocaleId.DE);
    }

    @Test
    public void testTransLanguage() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setTransLanguage(LocaleId.DE);
        assertThat(req.getTransLanguage()).isEqualTo(LocaleId.DE);
    }

    @Test
    public void testOptions() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        req.setOptions(options);
        assertThat(req.getOptions()).isEqualTo(options);
    }

    @Test
    public void testTexts() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        List<MSString> texts = new ArrayList<>();
        req.setTexts(texts);
        assertThat(req.getTexts()).isEqualTo(texts);
    }

    @Test
    public void testRoundTrip() throws JAXBException {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setSrcLanguage(LocaleId.EN_US);
        req.setTransLanguage(LocaleId.FR);
        req.getTexts().add(new MSString("test"));
        req.getTexts().add(new MSString("test1"));
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setContentType("xml");
        req.setOptions(options);
        String xml = DTOUtil.toXML(req);
        MSTranslateArrayReq req1 = DTOUtil.toObject(xml, MSTranslateArrayReq.class);
        assertThat(req).isEqualTo(req1);
    }

    @Test
    public void testEqualsAndHashCode() {
        MSTranslateArrayReq req1 = getDefaultRequest();

        MSTranslateArrayReq req2 = new MSTranslateArrayReq();
        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        // change appId
        req2 = getDefaultRequest();
        req2.setAppId(null);

        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        // change src lang
        req2 = getDefaultRequest();
        req2.setSrcLanguage(LocaleId.EN);

        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        // change trans lang
        req2 = getDefaultRequest();
        req2.setTransLanguage(LocaleId.DE);

        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        // change texts
        req2 = getDefaultRequest();
        req2.getTexts().add(new MSString("test1"));
        req2.getTexts().add(new MSString("test1"));

        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        // change option content type
        req2 = getDefaultRequest();
        req2.getOptions().setContentType("texts");

        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        req2 = getDefaultRequest();
        assertThat(req1.hashCode()).isEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isTrue();
    }

    private MSTranslateArrayReq getDefaultRequest() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setAppId("appId");
        req.setSrcLanguage(LocaleId.EN_US);
        req.setTransLanguage(LocaleId.FR);
        req.getTexts().add(new MSString("test"));
        req.getTexts().add(new MSString("test1"));
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setContentType("xml");
        req.setOptions(options);

        return req;
    }
}
