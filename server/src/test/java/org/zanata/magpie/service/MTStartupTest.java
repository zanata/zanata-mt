package org.zanata.magpie.service;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;

import com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTStartupTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock private AccountService accountService;
    private ConfigurationService config;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        File googleADC = temporaryFolder.newFile();
        config = new ConfigurationService("azureKey", googleADC.getAbsolutePath(), "{}", "ms");
    }

    @Test
    public void willCreateInitialPasswordIfNoAccountExists() throws IOException {
        MTStartup app = new MTStartup(config, accountService);
        app.onStartUp(null, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(app.initialPassword()).isNotBlank();
    }

    @Test
    public void willNotCreateInitialPasswordIfThereAreAccounts() {
        when(accountService.getAllAccounts(true)).thenReturn(
                newArrayList(new AccountDto()));

        MTStartup app = new MTStartup(config, accountService);
        app.onStartUp(null, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(app.initialPassword()).isNull();
    }
}
