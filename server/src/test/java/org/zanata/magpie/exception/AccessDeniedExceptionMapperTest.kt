package org.zanata.magpie.exception

import org.apache.deltaspike.security.api.authorization.AccessDeniedException
import org.apache.deltaspike.security.api.authorization.SecurityViolation
import org.assertj.core.api.Assertions
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.zanata.magpie.api.dto.APIResponse
import javax.ws.rs.core.Response

class AccessDeniedExceptionMapperTest {
    private lateinit var accessDeniedExceptionMapper: AccessDeniedExceptionMapper

    @Before
    fun setUp() {
        accessDeniedExceptionMapper = AccessDeniedExceptionMapper()
    }

    @Test
    fun canReturnForbidden() {
        val response = accessDeniedExceptionMapper.toResponse(AccessDeniedException(setOf(SecurityViolation { "missing role" })))

        Assertions.assertThat(response.status).isEqualTo(Response.Status.FORBIDDEN.statusCode)
        val entity = response.entity
        Assertions.assertThat(entity).isInstanceOf(APIResponse::class.java)

        val apiResponse: APIResponse = entity as APIResponse
        Assertions.assertThat(apiResponse.title).contains("missing role")

    }
}
