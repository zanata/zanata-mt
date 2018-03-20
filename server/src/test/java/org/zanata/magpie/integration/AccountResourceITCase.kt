package org.zanata.magpie.integration

import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.magpie.api.dto.AccountDto
import org.zanata.magpie.api.dto.CredentialDto
import org.zanata.magpie.model.AccountType
import java.util.*
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType
import javax.ws.rs.core.Response

class AccountResourceITCase {
    @Rule
    @JvmField
    val ensureAdminRule = EnsureAdminAccountRule

    @Test
    fun canUseAdminAccountToQueryAccounts() {
        val allAccounts = RestTest.setCommonHeadersAsAdmin(RestTest.newClient("account")).get()

        Assertions.assertThat(allAccounts.status).isEqualTo(200)
        val entityType: GenericType<List<AccountDto>> = object : GenericType<List<AccountDto>>() {}
        val entity = allAccounts.readEntity(entityType)
        Assertions.assertThat(entity.size).isGreaterThan(0)
    }

    @Test
    fun nonAdminUserCanNotCallAccountAPI() {
        val username = System.currentTimeMillis().toString()
        // create an non admin user first
        val accountDto = AccountDto(null, "User", username + "@example.com",
                AccountType.Normal, setOf(), setOf(
                CredentialDto(username, "password".toCharArray())
        ))
        val response = RestTest.setCommonHeadersAsAdmin(RestTest.newClient("account"))
                .post(Entity.json(accountDto))

        Assertions.assertThat(response.status).isEqualTo(201)
        response.close()

        val getResponse = RestTest.setCommonHeaders(RestTest.newClient("account"), username, "password").get()

        Assertions.assertThat(getResponse.status).isEqualTo(Response.Status.FORBIDDEN.statusCode)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(AccountResourceITCase::class.java)
    }
}
