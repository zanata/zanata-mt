package org.zanata.mt.service;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.mt.backend.google.GoogleCredential;
import org.zanata.mt.model.BackendID;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ConfigurationServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testConstructor() throws IOException {
        File googleADC = temporaryFolder.newFile();
        ConfigurationService config =
                new ConfigurationService("id", "key", "clientSubscriptionKey",
                        googleADC.getAbsolutePath(), "{\"type\": \"service_account\"," +
                        "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\"}", "ms");
        assertThat(config.getId()).isEqualTo("id");
        assertThat(config.getApiKey()).isEqualTo("key");
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
        ConfigurationService config = new ConfigurationService("id", "key", "",
                googleADC.getAbsolutePath(), "", "ms");
        assertThat(config.isDevMode()).isTrue();
    }

    @Test
    public void testAvailableProviders() throws IOException {
        File googleADC = temporaryFolder.newFile();

        ConfigurationService config =
                new ConfigurationService("id", "key", "clientSubscriptionKey",
                        googleADC.getAbsolutePath(),
                        "{\"type\": \"service_account\"," +
                                "\"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\"}",
                        "ms");

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
