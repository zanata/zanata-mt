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
        val docUrl = "http://example.com/doc/1"
        val originalString = TypeString("hello", "text/plain", "meta")
        val requestedLocale = "fr-FR"
        val actualLocale = "fr"

        val client = setCommonHeadersAsAdmin(RestTest.newClient("document/translate")
                .queryParam("toLocaleCode", requestedLocale))
        val response = client.post(Entity.json(DocumentContent(listOf(originalString), docUrl, "en-US")))

        Assertions.assertThat(response.status).isEqualTo(200)
        val entity = response.readEntity(DocumentContent::class.java)
        Assertions.assertThat(entity.backendId).isEqualTo("DEV")
        Assertions.assertThat(entity.localeCode).isEqualTo(actualLocale)
        Assertions.assertThat(entity.url).isEqualTo(docUrl)
        val expectedTranslation = TypeString("translated[网 hello 网]", "text/plain", "meta")
        Assertions.assertThat(entity.contents).containsExactly(expectedTranslation)
    }

}
