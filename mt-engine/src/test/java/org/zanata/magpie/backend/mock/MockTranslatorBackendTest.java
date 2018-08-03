package org.zanata.magpie.backend.mock;

import java.util.Optional;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.StringType;
import com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void testTranslatePlainTextSimple() {
        String content = "testing";
        BackendLocaleCode fromLocale =
                mockBackend.getMappedLocale(LocaleCode.EN);
        BackendLocaleCode toLocale =
                mockBackend.getMappedLocale(LocaleCode.DE);
        MediaType mediaType = MediaType.TEXT_PLAIN_TYPE;

        AugmentedTranslation translation =
                mockBackend.translate(ImmutableList.of(content), fromLocale, toLocale,
                        StringType.TEXT_PLAIN,
                        Optional.of("tech")).get(0);
        assertThat(translation.getRawTranslation())
                .isEqualTo("translated[网 testing 网]");
        assertThat(translation.getPlainTranslation())
                .isEqualTo("translated[网 testing 网]");
    }

    @Test
    public void testTranslateHtmlSimple() {
        String content = "testing";
        BackendLocaleCode fromLocale =
                mockBackend.getMappedLocale(LocaleCode.EN);
        BackendLocaleCode toLocale =
                mockBackend.getMappedLocale(LocaleCode.DE);

        AugmentedTranslation translation =
                mockBackend.translate(ImmutableList.of(content), fromLocale, toLocale, StringType.HTML,
                        Optional.of("tech")).get(0);
        assertThat(translation.getRawTranslation())
                .isEqualTo("translated[网 testing 网]");
        assertThat(translation.getPlainTranslation())
                .isEqualTo("translated[网 testing 网]");
    }

    @Test
    public void testTranslateHtml() {
        String content = "testing <strong>this</strong> text";
        BackendLocaleCode fromLocale =
                mockBackend.getMappedLocale(LocaleCode.EN);
        BackendLocaleCode toLocale =
                mockBackend.getMappedLocale(LocaleCode.DE);

        AugmentedTranslation translation =
                mockBackend.translate(ImmutableList.of(content), fromLocale, toLocale,
                        StringType.HTML,
                        Optional.of("tech")).get(0);
        assertThat(translation.getRawTranslation())
                .isEqualTo("translated[网 testing <strong>this</strong> text 网]");
        assertThat(translation.getPlainTranslation())
                .isEqualTo("translated[网 testing <strong>this</strong> text 网]");
    }

    @Test
    public void testTranslateHtmlComplex() {
        String content = "The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>]";
        BackendLocaleCode fromLocale =
                mockBackend.getMappedLocale(LocaleCode.EN);
        BackendLocaleCode toLocale =
                mockBackend.getMappedLocale(LocaleCode.DE);

        AugmentedTranslation translation =
                mockBackend.translate(ImmutableList.of(content), fromLocale, toLocale,
                        StringType.HTML,
                        Optional.of("tech")).get(0);
        assertThat(translation.getRawTranslation())
                .isEqualTo("translated[网 The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>] 网]");
        assertThat(translation.getPlainTranslation())
                .isEqualTo("translated[网 The <literal>@watch</literal> annotation is not working with accumulate in rules. [<link xlink:href=\"https://issues.jboss.org/browse/RHDM-509\">RHDM-509</link>] 网]");
    }

}
