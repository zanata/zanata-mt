package org.zanata.magpie.api.security

import org.assertj.core.api.Assertions.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.*
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.zanata.magpie.api.APIConstant
import org.zanata.magpie.api.AuthenticatedAccount
import org.zanata.magpie.api.service.impl.AccountResourceImpl
import org.zanata.magpie.api.service.impl.AccountResourceImpl.AUTH_HEADER
import org.zanata.magpie.model.Account
import org.zanata.magpie.service.AccountService
import java.net.URI
import java.util.*
import javax.inject.Provider
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.core.Cookie
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriInfo
import kotlin.collections.HashMap

class SecurityInterceptorTest {
    @Mock
    private lateinit var initialPasswordProvider: Provider<String>

    @Mock
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var accountResource: AccountResourceImpl

    @Mock private lateinit var uriInfo: UriInfo

    private lateinit var authenticatedAccount: AuthenticatedAccount

    @Mock
    private lateinit var requestContext: ContainerRequestContext

    @Captor
    private lateinit var responseCaptor: ArgumentCaptor<Response>

    private val headerUser = APIConstant.HEADER_USERNAME
    private val headerToken = APIConstant.HEADER_API_KEY

    private lateinit var interceptor: SecurityInterceptor

    var notPublicURI = URI("http://localhost/api/test")

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        authenticatedAccount = AuthenticatedAccount()
        interceptor = SecurityInterceptor(initialPasswordProvider, accountService, authenticatedAccount, accountResource)

        given(requestContext.getUriInfo()).willReturn(uriInfo)
        given(uriInfo.getRequestUri()).willReturn(notPublicURI)
    }

    @Test
    fun requestPublicAPI() {
        var uri = URI("http://localhost/api/info")
        given(uriInfo.getRequestUri()).willReturn(uri)
        interceptor.filter(requestContext)

        Mockito.verify(requestContext, Mockito.times(1)).getUriInfo()
        Mockito.verifyNoMoreInteractions(requestContext)
        Mockito.verifyZeroInteractions(accountService)
    }


    @Test
    fun requestWithoutUsernameIsUnauthenticated() {
        given(requestContext.getHeaderString(headerUser)).willReturn(null)

        interceptor.filter(requestContext)

        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun requestWithoutTokenIsUnauthenticated() {
        given(requestContext.getHeaderString(headerToken)).willReturn(null)

        interceptor.filter(requestContext)

        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun initialPasswordUnmatchIsUnauthenticated() {
        given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        given(requestContext.getHeaderString(headerToken)).willReturn("unmatchedInitialPassword")
        given(initialPasswordProvider.get()).willReturn("initialPassword")

        interceptor.filter(requestContext)

        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun canAuthenticateUsingInitialPasswordToCreateAccount() {
        given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        given(requestContext.getHeaderString(headerToken)).willReturn("initialPassword")
        given(initialPasswordProvider.get()).willReturn("initialPassword")
        given(requestContext.uriInfo).willReturn(uriInfo)
        given(uriInfo.path).willReturn("/account")
        given(requestContext.method).willReturn("POST")

        interceptor.filter(requestContext)

        assertThat(authenticatedAccount.hasAuthenticatedAccount()).isTrue()
        assertThat(authenticatedAccount.authenticatedAccount).isPresent
        assertThat(authenticatedAccount.authenticatedAccount.get().hasRole("admin")).isTrue()
    }

    @Test
    fun canNotAuthenticateUsingInitialPasswordIfNotCreatingAccount() {
        given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        given(requestContext.getHeaderString(headerToken)).willReturn("initialPassword")
        given(initialPasswordProvider.get()).willReturn("initialPassword")
        given(requestContext.uriInfo).willReturn(uriInfo)
        given(uriInfo.path).willReturn("/account")
        given(requestContext.method).willReturn("GET")

        interceptor.filter(requestContext)


        Mockito.verify(requestContext).abortWith(responseCaptor.capture())

        assertThat(responseCaptor.value.status).isEqualTo(Response.Status.UNAUTHORIZED.statusCode)
    }

    @Test
    fun canAuthenticateUsingDatabase() {
        given(requestContext.getHeaderString(headerUser)).willReturn("admin")
        given(requestContext.getHeaderString(headerToken)).willReturn("password")
        given(initialPasswordProvider.get()).willReturn(null)
        val account = Account()
        given(accountService.authenticate("admin", "password"))
                .willReturn(Optional.of(account))

        interceptor.filter(requestContext)

        assertThat(authenticatedAccount.hasAuthenticatedAccount()).isTrue()
        assertThat(authenticatedAccount.authenticatedAccount).isPresent
        assertThat(authenticatedAccount.authenticatedAccount.get()).isSameAs(account)
    }

    @Test
    fun canAuthenticateUsingCookie() {
        var cookie = Cookie("auth", "value", "/", "/")
        var cookies: HashMap<String, Cookie> = hashMapOf(AUTH_HEADER to cookie)
        given(requestContext.cookies).willReturn(cookies)
        val response = Response.ok().build()
        given(accountResource.login("value")).willReturn(response)

        interceptor.filter(requestContext)
        Mockito.verifyZeroInteractions(accountService)
    }
}
