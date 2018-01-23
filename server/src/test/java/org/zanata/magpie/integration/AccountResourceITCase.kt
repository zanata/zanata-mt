package org.zanata.magpie.integration

import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.magpie.api.dto.AccountDto
import javax.ws.rs.core.GenericType

class AccountResourceITCase {
    @Rule @JvmField val ensureAdminRule = EnsureAdminAccountRule

    @Test
    fun canUseAdminAccountToQueryAccounts() {
        val allAccounts = RestTest.setCommonHeadersAsAdmin(RestTest.newClient("account")).get()

        Assertions.assertThat(allAccounts.status).isEqualTo(200)
        val entityType: GenericType<List<AccountDto>> = object : GenericType<List<AccountDto>>() {}
        val entity = allAccounts.readEntity(entityType)
        Assertions.assertThat(entity).hasSize(1)
    }

    @Test
    fun nonAdminUserCanNotCallAccountAPI() {

    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AccountResourceITCase::class.java)
    }
}
