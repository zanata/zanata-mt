package org.zanata.magpie.integration

import org.assertj.core.api.Assertions.assertThat
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
    fun translateTextDocument() {
        val docUrl = "http://example.com/doc/1"
        val originalString = TypeString("hello", "text/plain", "meta")
        val requestedLocale = "fr-FR"
        val actualLocale = "fr"

        val client = setCommonHeadersAsAdmin(RestTest.newClient("document/translate")
                .queryParam("toLocaleCode", requestedLocale))
        val response = client.post(Entity.json(DocumentContent(listOf(originalString), docUrl, "en-US")))

        assertThat(response.status).isEqualTo(200)
        val entity = response.readEntity(DocumentContent::class.java)
        assertThat(entity.backendId).isEqualTo("DEV")
        assertThat(entity.localeCode).isEqualTo(actualLocale)
        assertThat(entity.url).isEqualTo(docUrl)
        val expectedTranslation = TypeString("translated[网 hello 网]", "text/plain", "meta")
        assertThat(entity.contents).containsExactly(expectedTranslation)
    }

    @Test
    fun translateHTMLDocument() {
        val docUrl = "http://example.com/doc/1"
        val originalString = TypeString("hello<br><b>world</b>", "text/html", "meta")
        val requestedLocale = "fr-FR"
        val actualLocale = "fr"

        val client = setCommonHeadersAsAdmin(RestTest.newClient("document/translate")
                .queryParam("toLocaleCode", requestedLocale))
        val response = client.post(Entity.json(DocumentContent(listOf(originalString), docUrl, "en-US")))

        assertThat(response.status).isEqualTo(200)
        val entity = response.readEntity(DocumentContent::class.java)
        assertThat(entity.backendId).isEqualTo("DEV")
        assertThat(entity.localeCode).isEqualTo(actualLocale)
        assertThat(entity.url).isEqualTo(docUrl)
        val expectedTranslation = TypeString("translated[网 hello<br><b>world</b> 网]", "text/html", "meta")
        assertThat(entity.contents).containsExactly(expectedTranslation)
    }

    @Test
    fun translateXMLDocument() {
        val docUrl = "http://example.com/doc/1"
        val originalString = TypeString("hello<br /><link>world</link>", "text/xml", "meta")
        val requestedLocale = "fr-FR"
        val actualLocale = "fr"

        val client = setCommonHeadersAsAdmin(RestTest.newClient("document/translate")
                .queryParam("toLocaleCode", requestedLocale))
        val response = client.post(Entity.json(DocumentContent(listOf(originalString), docUrl, "en-US")))

        assertThat(response.status).isEqualTo(200)
        val entity = response.readEntity(DocumentContent::class.java)
        assertThat(entity.backendId).isEqualTo("DEV")
        assertThat(entity.localeCode).isEqualTo(actualLocale)
        assertThat(entity.url).isEqualTo(docUrl)
        val expectedTranslation = TypeString("translated[网 hello<br /><link>world</link> 网]", "text/xml", "meta")
        assertThat(entity.contents).containsExactly(expectedTranslation)
    }

}
