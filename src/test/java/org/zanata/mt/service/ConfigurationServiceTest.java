package org.zanata.mt.service;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ConfigurationServiceTest {

    @Test
    public void testConstructor() {
        ConfigurationService config = new ConfigurationService("id", "key");
        assertThat(config.getId()).isEqualTo("id");
        assertThat(config.getApiKey()).isEqualTo("key");
        assertThat(config.getBuildDate()).isNotBlank();
        assertThat(config.getVersion()).isNotBlank();
    }
}
