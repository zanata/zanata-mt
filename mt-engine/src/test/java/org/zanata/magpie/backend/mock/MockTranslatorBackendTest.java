package org.zanata.magpie.backend.mock;

import org.junit.Before;
import org.junit.Test;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.model.AugmentedTranslation;

import javax.ws.rs.core.MediaType;

import java.util.Optional;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.magpie.backend.mock.MockTranslatorBackend.PREFIX_MOCK_STRING;
import static org.zanata.magpie.backend.mock.MockTranslatorBackend.UNICODE_SUPPLEMENTARY;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MockTranslatorBackendTest {
    private MockTranslatorBackend mockBackend;

    @Before
    public void setup() {
        mockBackend = new MockTranslatorBackend();
    }

    @Test
    public void testGetMappedLocale() {
        LocaleCode localeCode = LocaleCode.ZH_HANS;
        BackendLocaleCode backendLocaleCode =
                mockBackend.getMappedLocale(localeCode);
        assertThat(backendLocaleCode.getLocaleCode())
                .isEqualTo(localeCode.getId());

        localeCode = LocaleCode.DE;
        backendLocaleCode = mockBackend.getMappedLocale(localeCode);
        assertThat(backendLocaleCode.getLocaleCode())
                .isEqualTo(localeCode.getId());
    }

    @Test
    public void testTranslate() {
        String content = "testing";
        BackendLocaleCode fromLocale =
                mockBackend.getMappedLocale(LocaleCode.EN);
        BackendLocaleCode toLocale =
                mockBackend.getMappedLocale(LocaleCode.DE);
        MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

        AugmentedTranslation translation =
                mockBackend.translate(Lists.newArrayList(content), fromLocale, toLocale, mediaType,
                        Optional
                                .of("tech")).get(0);
        assertThat(translation.getRawTranslation())
                .contains(content, PREFIX_MOCK_STRING, UNICODE_SUPPLEMENTARY);
        assertThat(translation.getPlainTranslation())
                .contains(content, PREFIX_MOCK_STRING, UNICODE_SUPPLEMENTARY);
    }
}
