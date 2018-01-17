package org.zanata.magpie.service;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;

import com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTStartupTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock private AccountService accountService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEmptyCredentials() {
        ConfigurationService config = new ConfigurationService();
        MTStartup app = new MTStartup(config, accountService);
        assertThatThrownBy(() -> app.onStartUp(null, false, Sets.newHashSet()))
                .isInstanceOf(MTException.class);
    }

    @Test
    public void testOnStartup() throws IOException {
        File googleADC = temporaryFolder.newFile();
        ConfigurationService config =
                new ConfigurationService("id", "key", "azureKey", googleADC.getAbsolutePath(), "{}", "ms");
        MTStartup app = new MTStartup(config, accountService);
        app.onStartUp(null, false, Sets.newHashSet(BackendID.values()));
    }
}
