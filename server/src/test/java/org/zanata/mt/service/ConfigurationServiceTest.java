package org.zanata.mt.service;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.io.File;

import org.junit.Test;
import org.zanata.mt.model.BackendID;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ConfigurationServiceTest {

    @Test
    public void testConstructor() {
        ConfigurationService config = new ConfigurationService("id", "key",
                "clientSubscriptionKey", "googleKey", "ms");
        assertThat(config.getId()).isEqualTo("id");
        assertThat(config.getApiKey()).isEqualTo("key");
        assertThat(config.getMsAPIKey()).isEqualTo("clientSubscriptionKey");
        assertThat(config.googleDefaultCredentialFile())
                .isEqualTo(new File("googleKey"));
        assertThat(config.getBuildDate()).isNotBlank();
        assertThat(config.getVersion()).isNotBlank();
        assertThat(config.getDefaultTranslationProvider(false))
                .isEqualTo(BackendID.MS);
    }
}
