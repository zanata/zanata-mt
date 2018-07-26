package org.zanata.magpie.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.api.dto.CredentialDto;
import org.zanata.magpie.dao.AccountDAO;
import org.zanata.magpie.dao.CredentialDAO;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.Credential;
import org.zanata.magpie.model.LocalCredential;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.util.PasswordUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

public class AccountServiceTest {
    private AccountService service;
    @Mock
    private AccountDAO accountDAO;
    @Mock
    private CredentialDAO credentialDAO;
    @Captor
    private ArgumentCaptor<Credential> credentialCaptor;
    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    @Before
    public final void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new AccountService(accountDAO, credentialDAO);
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
                ImmutableList.of(account));

        List<AccountDto> result = service.getAllAccounts(false);
        assertThat(result).hasSize(1);
    }

    @Test
    public final void canGetAllAccounts() {
        Account account = new Account("joe", "joe@example.com", AccountType.Normal, Sets.newHashSet(Role.admin));

        given(accountDAO.findAll()).willReturn(ImmutableList.of(account));

        List<AccountDto> result = service.getAllAccounts(true);
        assertThat(result).hasSize(1);
    }

    @Test
    public final void canCreateNewAccount() {
        Account account = new Account();

        given(accountDAO.saveCredentialAndAccount(credentialCaptor.capture(),
                accountCaptor.capture())).willReturn(account);

        AccountDto accountDto = new AccountDto(null, "joe", "joe@example.com", AccountType.Normal, Sets.newHashSet(Role.admin));
        String secret = "secret";
        String username = "user";
        service.registerNewAccount(accountDto, username, secret.toCharArray());


        Credential credential = credentialCaptor.getValue();
        assertThat(credential.getUsername()).isEqualTo("user");
        assertThat(credential.getSecret()).startsWith((CharSequence)"$31$16");
    }

    @Test
    public void updateAccountIfCanFindAccountByEmailAndCredential() {
        Account account = new Account();
        Credential credential =
                new LocalCredential(account, "admin", "somepass");

        Set<Role> roles = Sets.newHashSet(Role.admin);
        char[] newSecret = "password".toCharArray();
        Set<CredentialDto> credentials = Sets.newHashSet(new CredentialDto("admin",
                newSecret));
        AccountDto accountDto = new AccountDto(null, "joe",
                "joe@example.com", AccountType.Normal, roles, credentials);

        given(accountDAO.findAccountByEmail("joe@example.com")).willReturn(Optional.of(account));
        given(credentialDAO.getCredentialByUsername("admin")).willReturn(Optional.of(credential));

        boolean result = service.updateAccount(accountDto);

        assertThat(result).isTrue();
        assertThat(account.getName()).isEqualTo("joe");
        assertThat(account.getAccountType()).isEqualTo(AccountType.Normal);
        assertThat(account.getRoles()).isEqualTo(roles);
        // secret will be updated to a hashed text
        assertThat(new PasswordUtil().authenticate(newSecret, credential.getSecret())).isTrue();
    }

    @Test
    public void updateAccountIfCanFindAccountByEmailAndSaveNewCredential() {
        Account account = new Account();

        Set<Role> roles = Sets.newHashSet(Role.admin);
        char[] newSecret = "password".toCharArray();
        Set<CredentialDto> credentials = Sets.newHashSet(new CredentialDto("admin",
                newSecret));
        AccountDto accountDto = new AccountDto(null, "joe",
                "joe@example.com", AccountType.Normal, roles, credentials);

        given(accountDAO.findAccountByEmail("joe@example.com")).willReturn(Optional.of(account));
        given(credentialDAO.getCredentialByUsername("admin")).willReturn(Optional.empty());

        boolean result = service.updateAccount(accountDto);

        assertThat(result).isTrue();
        assertThat(account.getName()).isEqualTo("joe");
        assertThat(account.getAccountType()).isEqualTo(AccountType.Normal);
        assertThat(account.getRoles()).isEqualTo(roles);
        assertThat(account.getCredentials()).hasSize(1);
        // secret will be updated to a hashed text
        Credential credential = account.getCredentials().iterator().next();
        assertThat(new PasswordUtil().authenticate(newSecret, credential.getSecret())).isTrue();
        verify(credentialDAO).persist(credentialCaptor.capture());
    }
}
