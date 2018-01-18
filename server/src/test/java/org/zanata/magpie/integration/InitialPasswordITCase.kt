package org.zanata.magpie.integration

import org.assertj.core.api.Assertions
import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.magpie.api.dto.AccountDto
import org.zanata.magpie.api.dto.CredentialDto
import org.zanata.magpie.model.AccountType
import org.zanata.magpie.model.Role
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType

class InitialPasswordITCase {


    @Test
    fun canUseInitialPasswordToCreateAccount() {
        // we can use initial password to authenticate as an admin and create a user
        val client = RestTest.setCommonHeaders(RestTest.newClient("account"), "admin", RestTest.initialPassword)
        val response = client.post(Entity.json(AccountDto(null, "Admin", "admin@example.com",
                AccountType.Normal, setOf(Role.admin),
                setOf(CredentialDto("admin", "secret".toCharArray())))))

        Assertions.assertThat(response.status).isEqualTo(201)
        response.close()

        // we can then use the new user to authenticate
        val allAccounts = RestTest.setCommonHeaders(RestTest.newClient("account"), "anotherAdmin", "secret").get()
        val entityType: GenericType<List<AccountDto>> = object : GenericType<List<AccountDto>>() {}
        val entity = allAccounts.readEntity(entityType)
        Assertions.assertThat(entity).hasSize(1)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(InitialPasswordITCase::class.java)
    }
}
