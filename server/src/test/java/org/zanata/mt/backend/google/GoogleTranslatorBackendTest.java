package org.zanata.mt.backend.google;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.backend.google.internal.dto.GoogleLocaleCode;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.service.ConfigurationService;

public class GoogleTranslatorBackendTest {

    private GoogleTranslatorBackend translatorBackend;

    @Before
    public void setUp() {
        translatorBackend = new GoogleTranslatorBackend(
                new ConfigurationService("", "", "", ""), false);
    }

    @Test
    public void canTranslateViaGoogle() {
        String googleAppCredentials =
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        Assume.assumeTrue("google application credential file exists",
                new File(googleAppCredentials).exists());
        AugmentedTranslation result =
                translatorBackend.translate("hello", new GoogleLocaleCode("en"),
                        new GoogleLocaleCode("zh"), MediaType.TEXT_PLAIN_TYPE);

        Assertions.assertThat(result.getPlainTranslation()).isEqualTo("你好");
    }

}
