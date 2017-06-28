package org.zanata.mt.backend.ms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.backend.ms.internal.dto.MSLocaleCode;
import org.zanata.mt.backend.ms.internal.dto.MSString;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResp;
import org.zanata.mt.backend.ms.internal.dto.MSTranslateArrayResponse;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.service.ConfigurationService;
import org.zanata.mt.util.DTOUtil;

import javax.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class MicrosoftTranslatorBackendTest {
    private MicrosoftTranslatorBackend msBackend = null;

    @Mock
    private ConfigurationService configurationService;

    @Test
    public void testConstructor() {
        msBackend = new MicrosoftTranslatorBackend();
    }

    @Test
    public void testVerifyKeyInvalid() {
        when(configurationService.isDevMode()).thenReturn(false);
        when(configurationService.getClientSubscriptionKey()).thenReturn(null);
        msBackend = new MicrosoftTranslatorBackend(configurationService);
        assertThatThrownBy(() -> msBackend.onInit(null))
                .isInstanceOf(ZanataMTException.class);
    }

    @Test
    public void testVerifyKey() {
        when(configurationService.isDevMode()).thenReturn(false);
        when(configurationService.getClientSubscriptionKey())
                .thenReturn("subscriptionKey");
        msBackend = new MicrosoftTranslatorBackend(configurationService);
        msBackend.onInit(null);
    }

    @Test
    public void testIgnoreKeyCheckingInDevMode() {
        when(configurationService.isDevMode()).thenReturn(true);
        when(configurationService.getClientSubscriptionKey()).thenReturn(null);
        msBackend = new MicrosoftTranslatorBackend(configurationService);
        msBackend.onInit(null);
    }

    @Test
    public void testMappedLocale() {
        when(configurationService.getClientSubscriptionKey())
                .thenReturn("subscriptionKey");
        LocaleCode from = LocaleCode.ZH_HANS;
        msBackend = new MicrosoftTranslatorBackend(configurationService);
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
        String responseString = DTOUtil.toXML(resp);

        MicrosoftTranslatorClient api =
                Mockito.mock(MicrosoftTranslatorClient.class);
        when(api.requestTranslations(any())).thenReturn(responseString);
        when(configurationService.getClientSubscriptionKey())
                .thenReturn("subscriptionKey");

        msBackend = new MicrosoftTranslatorBackend(configurationService);
        msBackend.setApi(api);
        AugmentedTranslation translation = msBackend
                .translate(content, srcLocale, transLocale,
                        MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"));
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
