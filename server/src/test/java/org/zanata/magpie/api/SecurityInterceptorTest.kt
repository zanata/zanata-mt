package org.zanata.magpie.api

import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.zanata.magpie.model.Account
import org.zanata.magpie.service.AccountService
import java.util.*
import javax.inject.Provider
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Response

class SecurityInterceptorTest {
    @Mock
    private lateinit var initialPasswordProvider: Provider<String>

    @Mock
    private lateinit var accountService: AccountService

    private lateinit var authenticatedAccount: AuthenticatedAccount

    @Mock
    private lateinit var requestContext: ContainerRequestContext

    @Captor
    private lateinit var responseCaptor: ArgumentCaptor<Response>

    private val headerUser = "X-Auth-User"
    private val headerToken = "X-Auth-Token"

    private lateinit var interceptor: SecurityInterceptor

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        authenticatedAccount = AuthenticatedAccount()
        interceptor = SecurityInterceptor(initialPasswordProvider, accountService, authenticatedAccount)
    }


    @Test
    fun requestWithoutUsernameIsUnauthenticated() {
        BDDMockito.given(requestContext.getHeaderString(headerUser)).willReturn(null)

        interceptor.filter(requestContext)

        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        Assertions.assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun requestWithoutTokenIsUnauthenticated() {
        BDDMockito.given(requestContext.getHeaderString(headerToken)).willReturn(null)

        interceptor.filter(requestContext)

        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        Assertions.assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun initialPasswordUnmatchIsUnauthenticated() {
        BDDMockito.given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        BDDMockito.given(requestContext.getHeaderString(headerToken)).willReturn("unmatchedInitialPassword")
        BDDMockito.given(initialPasswordProvider.get()).willReturn("initialPassword")

        interceptor.filter(requestContext)

        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        Assertions.assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun canAuthenticateUsingInitialPassword() {
        BDDMockito.given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        BDDMockito.given(requestContext.getHeaderString(headerToken)).willReturn("initialPassword")
        BDDMockito.given(initialPasswordProvider.get()).willReturn("initialPassword")

        interceptor.filter(requestContext)

        Assertions.assertThat(authenticatedAccount.hasAuthenticatedAccount()).isTrue()
        Assertions.assertThat(authenticatedAccount.authenticatedAccount).isPresent
        Assertions.assertThat(authenticatedAccount.authenticatedAccount.get().hasRole("admin")).isTrue()
    }

    @Test
    fun canAuthenticateUsingDatabase() {
        BDDMockito.given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        BDDMockito.given(requestContext.getHeaderString(headerToken)).willReturn("secret")
        BDDMockito.given(initialPasswordProvider.get()).willReturn(null)
        val account = Account()
        BDDMockito.given(accountService.authenticate("admin", "secret"))
                .willReturn(Optional.of(account))

        interceptor.filter(requestContext)

        Assertions.assertThat(authenticatedAccount.hasAuthenticatedAccount()).isTrue()
        Assertions.assertThat(authenticatedAccount.authenticatedAccount).isPresent
        Assertions.assertThat(authenticatedAccount.authenticatedAccount.get()).isSameAs(account)
    }
}
