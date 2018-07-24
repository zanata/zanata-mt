package org.zanata.magpie.service;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.magpie.backend.google.GoogleCredential;
import org.zanata.magpie.model.BackendID;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ConfigurationServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testEmptyConstructor() {
        assertThat(new ConfigurationService()).isNotNull();
    }

    @Test
    public void testConstructor() throws IOException {
        File googleADC = temporaryFolder.newFile();
        ConfigurationService config =
                new ConfigurationService("clientSubscriptionKey",
                        googleADC.getAbsolutePath(), "{\"type\": \"service_account\"," +
                        "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\"}", "ms", null);
        assertThat(config.getMsAPIKey()).isEqualTo("clientSubscriptionKey");
        assertThat(config.googleDefaultCredential().getCredentialsFile())
                .isEqualTo(googleADC);
        assertThat(config.getBuildDate()).isNotBlank();
        assertThat(config.getVersion()).isNotBlank();
        assertThat(config.getDefaultTranslationProvider(false))
                .isEqualTo(BackendID.MS);
    }

    @Test
    public void isDevModeIfNoAzureKeyAndGoogleCredential() throws IOException {
        File googleADC = temporaryFolder.newFile();
        ConfigurationService config = new ConfigurationService("",
                googleADC.getAbsolutePath(), "", "ms", null);
        assertThat(config.isDevMode()).isTrue();
    }

    @Test
    public void testAvailableProviders() throws IOException {
        File googleADC = temporaryFolder.newFile();

        ConfigurationService config =
                new ConfigurationService("clientSubscriptionKey",
                        googleADC.getAbsolutePath(),
                        "{\"type\": \"service_account\"," +
                                "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\"}",
                        "ms", "enableDev");

        assertThat(
                config.availableProviders(GoogleCredential.ABSENT, "", false))
                        .isEmpty();
        assertThat(config.availableProviders(config.googleDefaultCredential(),
                "", false)).containsExactly(BackendID.GOOGLE);
        assertThat(config.availableProviders(config.googleDefaultCredential(),
                "msKey", false)).containsExactlyInAnyOrder(BackendID.GOOGLE,
                        BackendID.MS);
        assertThat(config.availableProviders(GoogleCredential.ABSENT, "msKey",
                false)).containsExactly(BackendID.MS);
        assertThat(config.availableProviders(GoogleCredential.ABSENT, "msKey",
                true)).containsExactlyInAnyOrder(BackendID.MS, BackendID.DEV);
        assertThat(config.availableProviders(config.googleDefaultCredential(),
                "msKey", true)).containsExactlyInAnyOrder(BackendID.GOOGLE, BackendID.MS,
                        BackendID.DEV);
        assertThat(config.availableProviders(config.googleDefaultCredential(),
                "msKey", false)).containsExactlyInAnyOrder(BackendID.GOOGLE,
                        BackendID.MS);
    }
}
