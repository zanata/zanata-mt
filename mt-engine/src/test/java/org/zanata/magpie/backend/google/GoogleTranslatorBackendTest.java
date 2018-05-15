package org.zanata.magpie.backend.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.util.DTOUtil;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;

public class GoogleTranslatorBackendTest {
    @Mock
    private Translate translate;
    private GoogleTranslatorBackend backend;
    @Mock
    private DTOUtil dtoUtil;
    @Mock
    private Translation translation;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        backend = new GoogleTranslatorBackend(translate,
                dtoUtil, new GoogleCredential(new File(".")));
        when(dtoUtil.toJSON(translate)).thenReturn("{}");
        when(translation.getTranslatedText()).thenReturn("你好");
    }

    @Test
    public void canGetGoogleMappedLocales() {
        assertThat(backend.getMappedLocale(LocaleCode.EN_US))
                .isEqualTo(new BackendLocaleCodeImpl("en"));
        assertThat(backend.getMappedLocale(LocaleCode.ZH_HANS))
                .isEqualTo(new BackendLocaleCodeImpl("zh-CN"));
        assertThat(backend.getMappedLocale(LocaleCode.ZH_HANT))
                .isEqualTo(new BackendLocaleCodeImpl("zh-TW"));
        assertThat(backend.getMappedLocale(LocaleCode.DE))
                .isEqualTo(new BackendLocaleCodeImpl("de"));
    }

    @Test
    public void canTranslateHTML() {
        List<String> source = Lists.newArrayList("hello");
        List<Translation> expectedTrans = Lists.newArrayList(translation);

        when(translate.translate(source,
                Translate.TranslateOption.targetLanguage("zh"),
                Translate.TranslateOption.format("html")))
                        .thenReturn(expectedTrans);
        List<AugmentedTranslation> translations = backend.translate(source,
                new BackendLocaleCodeImpl("en"), new BackendLocaleCodeImpl("zh"),
                MediaType.TEXT_HTML_TYPE, Optional.empty());

        assertThat(translations).hasSize(expectedTrans.size());
        AugmentedTranslation augmentedTranslation = translations.get(0);
        assertThat(augmentedTranslation.getPlainTranslation()).isEqualTo(translation.getTranslatedText());
    }

    @Test
    public void canTranslateText() {
        List<String> source = Lists.newArrayList("hello");
        List<Translation> expectedTrans = Lists.newArrayList(translation);

        when(translate.translate(source,
                Translate.TranslateOption.targetLanguage("zh"),
                Translate.TranslateOption.format("text")))
                .thenReturn(expectedTrans);
        List<AugmentedTranslation> translations = backend.translate(source,
                new BackendLocaleCodeImpl("en"), new BackendLocaleCodeImpl("zh"),
                MediaType.TEXT_PLAIN_TYPE, Optional.empty());

        assertThat(translations).hasSize(expectedTrans.size());
        AugmentedTranslation augmentedTranslation = translations.get(0);
        assertThat(augmentedTranslation.getPlainTranslation()).isEqualTo(translation.getTranslatedText());
    }

}
