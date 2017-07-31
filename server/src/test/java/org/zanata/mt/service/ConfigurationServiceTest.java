package org.zanata.mt.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.zanata.mt.model.BackendID;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ConfigurationServiceTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testConstructor() throws IOException {
        File googkeADC = temporaryFolder.newFile();
        ConfigurationService config =
                new ConfigurationService("id", "key", "clientSubscriptionKey",
                        googkeADC.getAbsolutePath(), "{}", "ms");
        assertThat(config.getId()).isEqualTo("id");
        assertThat(config.getApiKey()).isEqualTo("key");
        assertThat(config.getMsAPIKey()).isEqualTo("clientSubscriptionKey");
        assertThat(config.googleDefaultCredentialFile())
                .isEqualTo(googkeADC);
        assertThat(config.getBuildDate()).isNotBlank();
        assertThat(config.getVersion()).isNotBlank();
        assertThat(config.getDefaultTranslationProvider(false))
                .isEqualTo(BackendID.MS);
    }

    @Test
    public void isDevModeIfNoAzureKeyAndGoogleCredential() throws IOException {
        File googkeADC = temporaryFolder.newFile();
        ConfigurationService config =
                new ConfigurationService("id", "key", "",
                        googkeADC.getAbsolutePath(), "", "ms");
        assertThat(config.isDevMode()).isTrue();
    }

    @Test
    public void googleCredentialFileMustExistIfGivingCredentialContent() {
        assertThatThrownBy(() -> new ConfigurationService("id", "key", "msKey", "/Non/exist/file/path", "{}", "ms"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("/Non/exist/file/path is not a valid file path");
    }

    @Test
    public void googleCredentialFileMayNotExistIfNoCredentialContent() {
        ConfigurationService config =
                new ConfigurationService("id", "key", "msKey",
                        "/Non/exist/file/path", "", "ms");

        assertThat(config.isDevMode()).isFalse();
        assertThat(config.googleDefaultCredentialFile()).doesNotExist();
        assertThat(config.getMsAPIKey()).isEqualTo("msKey");

    }
}
