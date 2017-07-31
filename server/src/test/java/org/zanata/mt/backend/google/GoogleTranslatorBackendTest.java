package org.zanata.mt.backend.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.google.internal.dto.GoogleLocaleCode;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.model.BackendID;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.Translation;

public class GoogleTranslatorBackendTest {
    @Mock
    private Translate translate;
    private File credentialFile = new File(System.getProperty("user.home"));
    private GoogleTranslatorBackend backend;
    private static Translation translation;

    @BeforeClass
    public static void createGoogleTranslationResult()
            throws IllegalAccessException, InvocationTargetException,
            InstantiationException {
        // TODO once we migrate DTOUtil into a CDI bean, we should get rid of this hack
        Constructor<?>[] constructors =
                Translation.class.getDeclaredConstructors();
        // there is only one private constructor
        Constructor<?> constructor = constructors[0];
        constructor.setAccessible(true);
        translation =
                (Translation) constructor.newInstance("hola", "en", "nmt");
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        // by default backend is created with dev mode false and a fake existing
        // credential file
        backend = new GoogleTranslatorBackend(translate, credentialFile, false);
    }

    @Test
    public void canNotCreateIfCredentialFileDoesNotExist() {
        assertThatThrownBy(() -> new GoogleTranslatorBackend(translate,
                new File("not-exist-file"), false))
                        .isInstanceOf(ZanataMTException.class).hasMessage(
                                "google application default credential is not defined");
    }

    @Test
    public void canCreateIfDevModeIsTrueEvenCredentialFileDoesNotExist() {
        GoogleTranslatorBackend backend = new GoogleTranslatorBackend(translate,
                new File("not-exist-file"), true);
        assertThat(backend.getId()).isEqualTo(BackendID.GOOGLE);
    }

    @Test
    public void canGetGoogleMappedLocales() {
        assertThat(backend.getMappedLocale(LocaleCode.EN_US))
                .isEqualTo(new GoogleLocaleCode("en"));
        assertThat(backend.getMappedLocale(LocaleCode.ZH_HANS))
                .isEqualTo(new GoogleLocaleCode("zh-CN"));
        assertThat(backend.getMappedLocale(LocaleCode.ZH_HANT))
                .isEqualTo(new GoogleLocaleCode("zh-TW"));
        assertThat(backend.getMappedLocale(LocaleCode.DE))
                .isEqualTo(new GoogleLocaleCode("de"));
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
                new GoogleLocaleCode("en"), new GoogleLocaleCode("zh"),
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
                new GoogleLocaleCode("en"), new GoogleLocaleCode("zh"),
                MediaType.TEXT_PLAIN_TYPE, Optional.empty());

        assertThat(translations).hasSize(expectedTrans.size());
        AugmentedTranslation augmentedTranslation = translations.get(0);
        assertThat(augmentedTranslation.getPlainTranslation()).isEqualTo(translation.getTranslatedText());
    }

}
