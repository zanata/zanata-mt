package org.zanata.mt.backend.ms;

import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResponse;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.model.Locale;
import org.zanata.mt.util.DTOUtil;

import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MicrosoftTranslatorBackendTest {
    private MicrosoftTranslatorBackend msBackend = null;

    @Test
    public void testConstructor() {
        msBackend = new MicrosoftTranslatorBackend();
    }

    @Test
    public void testVerifyCredentialsInvalid() {
        msBackend = new MicrosoftTranslatorBackend(null);
        assertThatThrownBy(() -> msBackend.onInit(null))
            .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testVerifyCredentials() {
        msBackend = new MicrosoftTranslatorBackend("subscriptionKey");
        msBackend.onInit(null);
    }

    @Test
    public void testClientSecret() {
        String key = "subscriptionKey";
        msBackend = new MicrosoftTranslatorBackend(key);
        assertThat(msBackend.getClientSubscriptionKey()).isEqualTo(key);
    }

    @Test
    public void testMappedLocale() {
        LocaleId from = LocaleId.ZH_HANS;
        msBackend = new MicrosoftTranslatorBackend("subscriptionKey");
        BackendLocaleCode to = msBackend.getMappedLocale(from);
        assertThat(to.getLocaleCode()).isNotEqualTo(from.getId());

        from = LocaleId.PT;
        to = msBackend.getMappedLocale(from);
        assertThat(to.getLocaleCode()).isEqualTo(from.getId());
    }

    @Test
    public void testTranslate() {
        String content = "content";
        Locale srcLocale = new Locale(LocaleId.EN, "English");
        Locale transLocale = new Locale(LocaleId.DE, "German");
        MSTranslateArrayResp resp = new MSTranslateArrayResp();
        List<MSTranslateArrayResponse> respList = new ArrayList<>();
        respList.add(buildMSResponse("translation1"));

        resp.setResponse(respList);
        String responseString = DTOUtil.toXML(resp);

        MicrosoftTranslatorClient api = Mockito.mock(MicrosoftTranslatorClient.class);
        when(api.requestTranslations(any())).thenReturn(responseString);

        msBackend = new MicrosoftTranslatorBackend("subscriptionKey");
        msBackend.setApi(api);
        AugmentedTranslation
                translation = msBackend.translate(content, srcLocale, transLocale, MediaType.TEXT_PLAIN_TYPE);
        assertThat(translation.getPlainTranslation()).isEqualTo("translation1");
        assertThat(translation.getRawTranslation())
                .isEqualTo(DTOUtil.toXML(resp.getResponse().get(0)));
    }

    private MSTranslateArrayResponse buildMSResponse(String message) {
        MSTranslateArrayResponse resp = new MSTranslateArrayResponse();
        resp.setSrcLanguage("en");
        resp.setTranslatedText(new MSString(message));
        return resp;
    }
}
