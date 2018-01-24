package org.zanata.magpie.security

import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext
import org.apache.deltaspike.security.api.authorization.SecurityViolation
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.zanata.magpie.annotation.CheckRole
import org.zanata.magpie.api.AuthenticatedAccount
import org.zanata.magpie.model.Account
import org.zanata.magpie.model.Role

@CheckRole("admin")
class CheckRoleDecisionVoterTest {
    private lateinit var checkRoleDecisionVoter: CheckRoleDecisionVoter
    @Mock
    private lateinit var context: AccessDecisionVoterContext

    private lateinit var authenticatedAccount: AuthenticatedAccount

    val checkRoleAnno = this::class.java.getAnnotation(CheckRole::class.java)


    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        authenticatedAccount = AuthenticatedAccount()
        checkRoleDecisionVoter = CheckRoleDecisionVoter(authenticatedAccount)
    }

    @Test
    fun noViolationIfNoCheckRoleAnnotation() {
        authenticatedAccount.setAuthenticatedAccount(Account())
        BDDMockito.given(context.getMetaDataFor(CheckRole::class.java.name, CheckRole::class.java)).willReturn(null)

        val violation = mutableSetOf<SecurityViolation>()
        checkRoleDecisionVoter.checkPermission(context, violation)

        Assertions.assertThat(violation).isEmpty()
    }

    @Test
    fun canReturnNotAuthenticated() {
        BDDMockito.given(context.getMetaDataFor(CheckRole::class.java.name, CheckRole::class.java)).willReturn(checkRoleAnno)

        val violation = mutableSetOf<SecurityViolation>()
        checkRoleDecisionVoter.checkPermission(context, violation)

        Assertions.assertThat(violation).hasSize(1)
        Assertions.assertThat(violation.iterator().next().reason).isEqualTo("Not authenticated")
    }

    @Test
    fun canReturnNoPermissionViolationIfAuthenticatedAccountHasNoRole() {
        authenticatedAccount.setAuthenticatedAccount(Account())
        BDDMockito.given(context.getMetaDataFor(CheckRole::class.java.name, CheckRole::class.java)).willReturn(checkRoleAnno)

        val violation = mutableSetOf<SecurityViolation>()
        checkRoleDecisionVoter.checkPermission(context, violation)

        Assertions.assertThat(violation).hasSize(1)
        Assertions.assertThat(violation.iterator().next().reason).isEqualTo("You don't have the necessary access")
    }

    @Test
    fun noViolationIfAuthenticatedAccountHasRequiredRole() {
        val account = Account()
        account.roles.add(Role.admin)
        authenticatedAccount.setAuthenticatedAccount(account)
        BDDMockito.given(context.getMetaDataFor(CheckRole::class.java.name, CheckRole::class.java)).willReturn(checkRoleAnno)

        val violation = mutableSetOf<SecurityViolation>()
        checkRoleDecisionVoter.checkPermission(context, violation)

        Assertions.assertThat(violation).isEmpty()
    }

}
