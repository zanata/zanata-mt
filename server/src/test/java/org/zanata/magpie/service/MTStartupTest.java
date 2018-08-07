package org.zanata.magpie.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.magpie.service.MTStartup.INITIAL_PASSWORD_CACHE;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;

import org.assertj.core.api.Assertions;
import org.infinispan.Cache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.event.AccountCreated;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Role;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.magpie.service.MTStartup.INITIAL_PASSWORD_CACHE;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class MTStartupTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock private AccountService accountService;
    private ConfigurationService config;
    private MTStartup mtStartup;
    @Mock private Cache<String, String> replCache;
    @Mock private ServletContext context;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        File googleADC = temporaryFolder.newFile();
        config = new ConfigurationService("azureKey", googleADC.getAbsolutePath(), "{}", "ms", null);
        mtStartup = new MTStartup(config, accountService, replCache);
    }

    @Test
    public void testEmptyConstructor() {
        assertThat(new MTStartup()).isNotNull();
    }

    @Test
    public void willCreateInitialPasswordIfNoAccountExists() throws IOException {
        mtStartup.onStartUp(context, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(mtStartup.getInitialPasswords()).isNotBlank();
    }

    @Test
    public void willNotCreateInitialPasswordIfThereAreAccounts() {
        when(accountService.getAllAccounts(true)).thenReturn(
                ImmutableList.of(new AccountDto()));

        mtStartup.onStartUp(context, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(mtStartup.getInitialPasswords()).isNull();
    }

    @Test
    public void willClearInitialPasswordIfAdminAccountIsCreated() {
        mtStartup.onStartUp(context, false, Sets.newHashSet(BackendID.values()));

        Assertions.assertThat(mtStartup.getInitialPasswords()).isNotBlank();

        when(replCache.get(INITIAL_PASSWORD_CACHE)).thenReturn("something");
        mtStartup.accountCreated(new AccountCreated("user@example.com", Sets.newHashSet(
                Role.admin)));

        verify(replCache).remove(INITIAL_PASSWORD_CACHE);
    }
}
