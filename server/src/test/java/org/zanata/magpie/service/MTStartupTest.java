package org.zanata.magpie.service;

import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.security.AccountCreated;

import com.google.common.collect.Sets;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTStartupTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock private AccountService accountService;
    private ConfigurationService config;
    private MTStartup mtStartup;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        File googleADC = temporaryFolder.newFile();
        config = new ConfigurationService("azureKey", googleADC.getAbsolutePath(), "{}", "ms");
        mtStartup = new MTStartup(config, accountService);
    }

    @Test
    public void willCreateInitialPasswordIfNoAccountExists() throws IOException {
        mtStartup.onStartUp(null, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(mtStartup.initialPassword()).isNotBlank();
    }

    @Test
    public void willNotCreateInitialPasswordIfThereAreAccounts() {
        when(accountService.getAllAccounts(true)).thenReturn(
                newArrayList(new AccountDto()));

        mtStartup.onStartUp(null, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(mtStartup.initialPassword()).isNull();
    }

    @Test
    public void willClearInitialPasswordIfAdminAccountIsCreated() {
        mtStartup.onStartUp(null, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(mtStartup.initialPassword()).isNotBlank();

        mtStartup.accountCreated(new AccountCreated("user@example.com", Sets.newHashSet(
                Role.admin)));

        Assertions.assertThat(mtStartup.initialPassword()).isNull();
    }
}
