package org.zanata.magpie.backend.ms;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.util.DTOUtil;

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
        BackendLocaleCode srcLocale = new BackendLocaleCodeImpl(LocaleCode.EN);
        BackendLocaleCode transLocale = new BackendLocaleCodeImpl(LocaleCode.DE);

        MicrosoftTranslatorClient api =
                Mockito.mock(MicrosoftTranslatorClient.class);

        when(api.requestTranslations(any(), srcLocale.getLocaleCode(),
                transLocale.getLocaleCode(), Optional.of("tech"), MediaType.TEXT_PLAIN_TYPE)).thenReturn("[{\"translations\":[{\"text\":\"どのようにている\",\"to\":\"ja\"}]},{\"translations\":[{\"text\":\"こんにちは\",\"to\":\"ja\"}]}]");

        msBackend = new MicrosoftTranslatorBackend("subscriptionKey", dtoUtil);
        msBackend.setApi(api);
        List<AugmentedTranslation> translations = msBackend
                .translate(Lists.newArrayList(content), srcLocale, transLocale,
                        MediaType.TEXT_PLAIN_TYPE, Optional.of("tech"));

        assertThat(translations).hasSize(1);
        AugmentedTranslation translation = translations.get(0);
        assertThat(translation.getPlainTranslation()).isEqualTo("どのようにている");
        assertThat(translation.getRawTranslation())
                .isEqualTo("{\"text\":\"どのようにている\",\"to\":\"ja\"}]},{\"translations\":[{\"text\":\"こんにちは\",\"to\":\"ja\"}");
    }
}
