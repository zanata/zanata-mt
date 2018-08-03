package org.zanata.magpie.dao;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.zanata.magpie.JPATest;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.Role;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountDAOTest extends JPATest {

    private AccountDAO accountDAO;

    @Override
    protected void setupTestData() {
        Account account =
            new Account("John Smith", "admin@example.com", "admin",
                Strings.repeat("abcde", 10), AccountType.Normal,
                Sets.newHashSet(Role.admin));
        getEm().persist(account);
    }

    @Before
    public void setUp() {
        accountDAO = new AccountDAO(getEm());
    }

    @Test
    public void canFindAccountByCredentialUsername() {
        Optional<Account> account =
                accountDAO.findAccountByUsername("admin");

        assertThat(account.isPresent()).isTrue();
    }

    @Test
    public void canFindAllAccounts() {
        List<Account> all = accountDAO.findAll();
        assertThat(all).hasSize(1);
    }

    @Test
    public void canFindAllEnabledAccounts() {
        List<Account> all = accountDAO.findAllEnabled();
        assertThat(all).hasSize(1);
    }

    @Test
    public void canSaveLocalUser() {
        Account account =
            new Account("user", "user@example.com", "username",
                "$31$16$ErL2RQyoK4C3N_0woVfE5De37d6t-XI1sIfEpldJl9I",
                AccountType.Normal, Sets.newHashSet(Role.user));
        account = accountDAO.persist(account);
        assertThat(account.getId()).isNotNull();
        assertThat(account.getUsername()).isEqualTo("username");
    }

    @Test
    public void canFindAccountByEmail() {

        Optional<Account> accountOpt = accountDAO.findAccountByEmail("admin@example.com");

        assertThat(accountOpt.isPresent()).isTrue();
        Account account = accountOpt.get();
        assertThat(account.getId()).isNotNull();
        assertThat(account.getName()).isEqualTo("John Smith");
    }
}
