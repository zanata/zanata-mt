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
        String googleAppCredentials =
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        Assume.assumeTrue("google application credential file exists",
                new File(googleAppCredentials).exists());
        AugmentedTranslation result = translatorBackend.translate("hello",
                new GoogleLocaleCode("en"), new GoogleLocaleCode("zh"),
                MediaType.TEXT_PLAIN_TYPE, Optional.empty());

        Assertions.assertThat(result.getPlainTranslation()).isEqualTo("你好");
    }

}
