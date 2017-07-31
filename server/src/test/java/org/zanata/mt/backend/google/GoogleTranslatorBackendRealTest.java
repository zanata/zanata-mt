package org.zanata.mt.backend.google;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.mt.backend.google.internal.dto.GoogleLocaleCode;
import org.zanata.mt.model.AugmentedTranslation;

import com.google.common.collect.Lists;

/**
 * This test will check for existence of environment variable
 * GOOGLE_APPLICATION_CREDENTIALS and perform a *REAL* call to google API if it
 * exists.
 */
public class GoogleTranslatorBackendRealTest {

    private static File credentialFile;
    private GoogleTranslatorBackend translatorBackend;

    @BeforeClass
    public static void checkCredential() {
        String googleAppCredentials =
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        Assume.assumeNotNull(googleAppCredentials);
        credentialFile = new File(googleAppCredentials);
        Assume.assumeTrue("google application credential file exists",
                credentialFile.exists());
    }

    @Before
    public void setUp() {
        translatorBackend = new GoogleTranslatorBackend(credentialFile, false);
    }

    @Test
    public void canTranslateViaGoogle() {
        AugmentedTranslation result = translateSingle(
                "<div>hello<a href='http://nowhere.com'>world</a></div>", "zh",
                MediaType.TEXT_HTML_TYPE);

        Assertions.assertThat(result.getPlainTranslation())
                .isEqualTo("<div>你好<a href='http://nowhere.com'>世界</a> </div>");
    }

    private AugmentedTranslation translateSingle(String content,
            String targetLocale, MediaType mediaType) {
        List<AugmentedTranslation> translations =
                translatorBackend.translate(Lists.newArrayList(content),
                        new GoogleLocaleCode("en"),
                        new GoogleLocaleCode(targetLocale), mediaType,
                        Optional.empty());
        return translations.get(0);
    }

    @Test
    public void canTranslatePlainText() {
        AugmentedTranslation result =
                translateSingle("Why &amp; is a nuisance in source code", "zh",
                        MediaType.TEXT_PLAIN_TYPE);

        Assertions.assertThat(result.getPlainTranslation())
                .isEqualTo("为什么＆amp;是源代码的滋扰");
    }

}
