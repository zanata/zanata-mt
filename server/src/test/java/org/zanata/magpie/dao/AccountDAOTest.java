package org.zanata.magpie.dao;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.zanata.magpie.JPATest;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.model.Credential;
import org.zanata.magpie.model.LocalCredential;
import org.zanata.magpie.model.Account;

import com.google.common.collect.Sets;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountDAOTest extends JPATest {

    private AccountDAO accountDAO;

    @Override
    protected void setupTestData() {
        Account account = new Account("John Smith", "admin@example.com", AccountType.Normal,
                Sets.newHashSet("admin"));
        LocalCredential localCredential = new LocalCredential(account, "admin", "secret");

        getEm().persist(account);
        getEm().persist(localCredential);
    }

    @Before
    public void setUp() {
        accountDAO = new AccountDAO(getEm());
    }

    @Test
    public void canFindUserByCredential() {
        Optional<Account> account =
                accountDAO.findUserWithCredential("admin", "secret");

        assertThat(account.isPresent()).isTrue();
    }

    @Test
    public void canSaveLocalUser() {
        Account account = accountDAO.saveLocalAccount("user", "user@example.com",
                AccountType.Normal, Sets.newHashSet("user"), "username",
                "password".toCharArray());

        assertThat(account.getId()).isNotNull();
        assertThat(account.getCredentials()).hasSize(1);
        Credential credential = account.getCredentials().iterator().next();
        assertThat(credential.getUsername()).isEqualTo("username");
        assertThat(credential.getSecret()).isNotEqualTo("password");
    }
}
