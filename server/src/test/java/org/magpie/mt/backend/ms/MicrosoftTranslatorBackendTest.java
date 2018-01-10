package org.magpie.mt.backend.ms;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.backend.BackendLocaleCode;
import org.magpie.mt.backend.ms.internal.dto.MSLocaleCode;
import org.magpie.mt.backend.ms.internal.dto.MSString;
import org.magpie.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.magpie.mt.backend.ms.internal.dto.MSTranslateArrayResponse;
import org.magpie.mt.model.AugmentedTranslation;
import org.magpie.mt.util.DTOUtil;

import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class MicrosoftTranslatorBackendTest {
    private MicrosoftTranslatorBackend msBackend = null;
    private DTOUtil dtoUtil = new DTOUtil();

    @Test
    public void testConstructor() {
        msBackend = new MicrosoftTranslatorBackend();
    }

    @Test
    public void testMappedLocale() {
        LocaleCode from = LocaleCode.ZH_HANS;
        msBackend = new MicrosoftTranslatorBackend("subscriptionKey", dtoUtil);
        BackendLocaleCode to = msBackend.getMappedLocale(from);
        assertThat(to.getLocaleCode()).isNotEqualTo(from.getId());

        from = LocaleCode.PT;
        to = msBackend.getMappedLocale(from);
        assertThat(to.getLocaleCode()).isEqualTo(from.getId());
    }

    @Test
    public void testTranslate() {
        String content = "content";
        MSLocaleCode srcLocale = new MSLocaleCode(LocaleCode.EN);
        MSLocaleCode transLocale = new MSLocaleCode(LocaleCode.DE);
        MSTranslateArrayResp resp = new MSTranslateArrayResp();
        List<MSTranslateArrayResponse> respList = new ArrayList<>();
        respList.add(buildMSResponse("translation1"));

        resp.setResponse(respList);
        String responseString = dtoUtil.toXML(resp);

        MicrosoftTranslatorClient api =
                Mockito.mock(MicrosoftTranslatorClient.class);
        when(api.requestTranslations(any())).thenReturn(responseString);

        msBackend = new MicrosoftTranslatorBackend("subscriptionKey", dtoUtil);
        msBackend.setApi(api);
        List<AugmentedTranslation> translations = msBackend
                .translate(Lists.newArrayList(content), srcLocale, transLocale,
                        MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"));

        assertThat(translations).hasSize(1);
        AugmentedTranslation translation = translations.get(0);
        assertThat(translation.getPlainTranslation()).isEqualTo("translation1");
        assertThat(translation.getRawTranslation())
                .isEqualTo(dtoUtil.toXML(resp.getResponse().get(0)));
    }

    private MSTranslateArrayResponse buildMSResponse(String message) {
        MSTranslateArrayResponse resp = new MSTranslateArrayResponse();
        resp.setSrcLanguage("en");
        resp.setTranslatedText(new MSString(message));
        return resp;
    }
}
