package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.dao.AccountDAO;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.Credential;
import org.zanata.magpie.model.LocalCredential;
import org.zanata.magpie.model.Role;

import com.google.common.collect.Sets;

public class AccountServiceTest {
    private AccountService service;
    @Mock
    private AccountDAO accountDAO;
    @Captor
    private ArgumentCaptor<Credential> crentialCaptor;
    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Before
    public final void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new AccountService(accountDAO);
    }

    @Test
    public final void canNotAuthenticateIfUsernameMatchesNothing() {

        given(accountDAO.findAccountByUsername("user")).willReturn(Optional.empty());

        Optional<Account> result = service.authenticate("user", "secret");
        assertThat(result).isEmpty();
    }

    @Test
    public final void canNotAuthenticateIfAccountCredentialCanNotMatchSecret() {
        Account account = new Account();
        account.setCredentials(Sets
                .newHashSet(new LocalCredential(account, "user", "$31$16$ErL2RQyoK4C3N_0woVfE5De37d6t-XI1sIfEpldJl9I")));

        given(accountDAO.findAccountByUsername("user")).willReturn(Optional.of(account));

        Optional<Account> result = service.authenticate("user", "notMatch");
        assertThat(result).isEmpty();
    }

    @Test
    public final void canAuthenticateIfAccountCredentialCanMatchSecret() {
        Account account = new Account();
        account.setCredentials(Sets.newHashSet(new LocalCredential(account, "user", "$31$16$ErL2RQyoK4C3N_0woVfE5De37d6t-XI1sIfEpldJl9I")));

        given(accountDAO.findAccountByUsername("user")).willReturn(Optional.of(account));

        Optional<Account> result = service.authenticate("user", "devKEY");
        assertThat(result).isPresent();
        assertThat(result.get()).isSameAs(account);
    }

    @Test
    public final void canGetAllEnabledAccounts() {
        Account account = new Account("joe", "joe@example.com", AccountType.Normal, Sets.newHashSet(Role.admin));

        given(accountDAO.findAllEnabled()).willReturn(
                Lists.newArrayList(account));

        List<AccountDto> result = service.getAllAccounts(false);
        assertThat(result).hasSize(1);
    }

    @Test
    public final void canGetAllAccounts() {
        Account account = new Account("joe", "joe@example.com", AccountType.Normal, Sets.newHashSet(Role.admin));

        given(accountDAO.findAll()).willReturn(Lists.newArrayList(account));

        List<AccountDto> result = service.getAllAccounts(true);
        assertThat(result).hasSize(1);
    }

    @Test
    public final void canCreateNewAccount() {
        Account account = new Account();

        given(accountDAO.saveCredentialAndAccount(crentialCaptor.capture(),
                accountCaptor.capture())).willReturn(account);

        AccountDto accountDto = new AccountDto(null, "joe", "joe@example.com", AccountType.Normal, Sets.newHashSet(Role.admin));
        String secret = "secret";
        String username = "user";
        service.registerNewAccount(accountDto, username, secret.toCharArray());


        Credential credential = crentialCaptor.getValue();
        assertThat(credential.getUsername()).isEqualTo("user");
        assertThat(credential.getSecret()).startsWith((CharSequence)"$31$16");
    }
}
