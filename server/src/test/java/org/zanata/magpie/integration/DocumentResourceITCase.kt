package org.zanata.magpie.integration

import org.assertj.core.api.Assertions
import org.junit.Rule
import org.junit.Test
import org.zanata.magpie.api.dto.DocumentContent
import org.zanata.magpie.api.dto.TypeString
import org.zanata.magpie.integration.RestTest.setCommonHeadersAsAdmin
import javax.ws.rs.client.Entity

/**
 * Integration test for document translation resource.
 */
class DocumentResourceITCase {
    @Rule @JvmField val ensureAdminRule = EnsureAdminAccountRule

    @Test
    fun translateDocument() {
        val client = setCommonHeadersAsAdmin(RestTest.newClient("document/translate")
                .queryParam("toLocaleCode", "fr"))

        val response = client.post(Entity.json(DocumentContent(listOf(TypeString("hello", "text/plain", "meta")), "http://example.com/doc/1", "en-US")))
        Assertions.assertThat(response.status).isEqualTo(200)
    }


}
