package org.zanata.magpie.service

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.zanata.magpie.api.dto.AccountDto
import org.zanata.magpie.dao.AccountDAO
import org.zanata.magpie.model.Account
import org.zanata.magpie.model.AccountType
import org.zanata.magpie.model.Credential
import org.zanata.magpie.model.LocalCredential
import org.zanata.magpie.model.Role
import java.util.*

class AccountServiceTest {
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var accountDAO: AccountDAO

    @Captor
    private lateinit var crentialCaptor: ArgumentCaptor<Credential>
    @Captor
    private lateinit var accountCaptor: ArgumentCaptor<Account>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        accountService = AccountService(accountDAO)
    }

    @Test
    fun canNotAuthenticateIfUsernameMatchesNothing() {
        BDDMockito.given(accountDAO.findAccountByUsername("user")).willReturn(Optional.empty())

        val result = accountService.authenticate("user", "secret")

        Assertions.assertThat(result).isEmpty
    }

    @Test
    fun canNotAuthenticateIfAccountCredentialCanNotMatchSecret() {
        val account = Account()
        account.credentials = setOf(
                LocalCredential(account, "user", "$31$16\$ErL2RQyoK4C3N_0woVfE5De37d6t-XI1sIfEpldJl9I"))
        BDDMockito.given(accountDAO.findAccountByUsername("user")).willReturn(Optional.of(account))

        val result = accountService.authenticate("user", "notMatch")

        Assertions.assertThat(result).isEmpty
    }

    @Test
    fun canAuthenticateIfAccountCredentialCanMatchSecret() {
        val account = Account()
        account.credentials = setOf(
                LocalCredential(account, "user", "$31$16\$ErL2RQyoK4C3N_0woVfE5De37d6t-XI1sIfEpldJl9I"))
        BDDMockito.given(accountDAO.findAccountByUsername("user")).willReturn(Optional.of(account))

        val result = accountService.authenticate("user", "devKEY")

        Assertions.assertThat(result).isPresent
        Assertions.assertThat(result.get()).isSameAs(account)

    }

    @Test
    fun canGetAllEnabledAccounts() {
        val account = Account("joe", "joe@example.com", AccountType.Normal, setOf(Role.admin))
        BDDMockito.given(accountDAO.findAllEnabled()).willReturn(listOf(account))

        val result = accountService.getAllAccounts(false)

        Assertions.assertThat(result).hasSize(1)
    }

    @Test
    fun canGetAllAccounts() {
        val account = Account("joe", "joe@example.com", AccountType.Normal, setOf(Role.admin))
        BDDMockito.given(accountDAO.findAll()).willReturn(listOf(account))

        val result = accountService.getAllAccounts(true)

        Assertions.assertThat(result).hasSize(1)
    }

    @Test
    fun canCreateNewAccount() {
        val account = Account()
        BDDMockito.given(accountDAO.saveCredentialAndAccount(crentialCaptor.capture(), accountCaptor.capture()))
                .willReturn(account)
        accountService.registerNewAccount(AccountDto(null, "joe", "joe@example.com", AccountType.Normal, setOf(Role.admin)), "user", "secret".toCharArray())

        val credential = crentialCaptor.value
        Assertions.assertThat(credential.username).isEqualTo("user")
        Assertions.assertThat(credential.secret).startsWith("$31$16")

    }


}
