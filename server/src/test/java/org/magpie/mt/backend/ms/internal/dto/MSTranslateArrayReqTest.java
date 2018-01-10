package org.magpie.mt.backend.ms.internal.dto;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.util.DTOUtil;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MSTranslateArrayReqTest {
    private DTOUtil dtoUtil = new DTOUtil();

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
        req.setSrcLanguage(LocaleCode.DE.getId());
        assertThat(req.getSrcLanguage()).isEqualTo(LocaleCode.DE.getId());
    }

    @Test
    public void testTransLanguage() {
        MSTranslateArrayReq req = new MSTranslateArrayReq();
        req.setTransLanguage(LocaleCode.DE.getId());
        assertThat(req.getTransLanguage()).isEqualTo(LocaleCode.DE.getId());
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
        req.setSrcLanguage(LocaleCode.EN_US.getId());
        req.setTransLanguage(LocaleCode.FR.getId());
        req.getTexts().add(new MSString("test"));
        req.getTexts().add(new MSString("test1"));
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setContentType("xml");
        req.setOptions(options);
        String xml = dtoUtil.toXML(req);
        MSTranslateArrayReq req1 = dtoUtil.toObject(xml, MSTranslateArrayReq.class);
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
        req2.setSrcLanguage(LocaleCode.EN.getId());

        assertThat(req1.hashCode()).isNotEqualTo(req2.hashCode());
        assertThat(req1.equals(req2)).isFalse();

        // change trans lang
        req2 = getDefaultRequest();
        req2.setTransLanguage(LocaleCode.DE.getId());

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
        req.setSrcLanguage(LocaleCode.EN_US.getId());
        req.setTransLanguage(LocaleCode.FR.getId());
        req.getTexts().add(new MSString("test"));
        req.getTexts().add(new MSString("test1"));
        MSTranslateArrayReqOptions options = new MSTranslateArrayReqOptions();
        options.setContentType("xml");
        req.setOptions(options);

        return req;
    }
}
