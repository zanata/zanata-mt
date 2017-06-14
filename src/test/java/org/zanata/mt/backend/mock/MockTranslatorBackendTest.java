package org.zanata.mt.backend.mock;

import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.model.AugmentedTranslation;

import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.zanata.mt.backend.mock.MockTranslatorBackend.PREFIX_MOCK_STRING;
import static org.zanata.mt.backend.mock.MockTranslatorBackend.UNICODE_SUPPLEMENTARY;

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
                mockBackend.translate(content, fromLocale, toLocale, mediaType);
        assertThat(translation.getRawTranslation())
                .contains(content, PREFIX_MOCK_STRING, UNICODE_SUPPLEMENTARY);
        assertThat(translation.getPlainTranslation())
                .contains(content, PREFIX_MOCK_STRING, UNICODE_SUPPLEMENTARY);
    }
}
