package org.zanata.mt.backend.google;

import java.io.File;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.mt.backend.google.internal.dto.GoogleLocaleCode;
import org.zanata.mt.model.AugmentedTranslation;

public class GoogleTranslatorBackendTest {

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
        AugmentedTranslation result = translatorBackend.translate(
                "<div>hello<a href='http://nowhere.com'>world</a></div>",
                new GoogleLocaleCode("en"), new GoogleLocaleCode("zh"),
                MediaType.TEXT_HTML_TYPE, Optional.empty());

        Assertions.assertThat(result.getPlainTranslation())
                .isEqualTo("<div>你好<a href='http://nowhere.com'>世界</a> </div>");
    }

    @Test
    public void canTranslatePlainText() {
        AugmentedTranslation result = translatorBackend.translate(
                "Why &amp; is a nuisance in source code",
                new GoogleLocaleCode("en"), new GoogleLocaleCode("zh"),
                MediaType.TEXT_PLAIN_TYPE, Optional.empty());

        Assertions.assertThat(result.getPlainTranslation())
                .isEqualTo("为什么＆amp;是源代码的滋扰");
    }



}
