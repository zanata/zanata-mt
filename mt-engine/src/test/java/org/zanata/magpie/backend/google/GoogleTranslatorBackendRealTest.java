package org.zanata.magpie.backend.google;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.magpie.backend.BackendLocaleCodeImpl;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.model.StringType;
import org.zanata.magpie.util.DTOUtil;
import com.google.common.collect.ImmutableList;

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
        translatorBackend = new GoogleTranslatorBackend(new DTOUtil(), new GoogleCredential(credentialFile));
    }

    @Test
    public void canTranslateViaGoogle() {
        AugmentedTranslation result = translateSingle(
                "<div>hello<a href='http://nowhere.com'>world</a></div>", "zh",
                StringType.HTML);

        Assertions.assertThat(result.getPlainTranslation())
                .isEqualTo("<div>你好<a href='http://nowhere.com'>世界</a> </div>");
    }

    private AugmentedTranslation translateSingle(String content,
            String targetLocale, StringType stringType) {
        List<AugmentedTranslation> translations =
                translatorBackend.translate(ImmutableList.of(content),
                        new BackendLocaleCodeImpl("en"),
                        new BackendLocaleCodeImpl(targetLocale), stringType,
                        Optional.empty());
        return translations.get(0);
    }

    @Test
    public void canTranslatePlainText() {
        AugmentedTranslation result =
                translateSingle("Why &amp; is a nuisance in source code", "zh",
                        StringType.TEXT_PLAIN);

        Assertions.assertThat(result.getPlainTranslation())
                .isEqualTo("为什么＆amp;是源代码的滋扰");
    }

}
