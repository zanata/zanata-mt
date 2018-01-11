package org.zanata.magpie.integration

import org.assertj.core.api.Assertions
import org.junit.Test
import org.zanata.magpie.api.dto.DocumentContent
import org.zanata.magpie.api.dto.TypeString
import org.zanata.magpie.integration.RestTest.setCommonHeaders
import javax.ws.rs.client.Entity

class DocProcessManagerITCase {

    @Test
    fun translateDocument() {
        val client = setCommonHeaders(RestTest.newClient("document/translate")
                .queryParam("toLocaleCode", "fr"))

        val response = client.post(Entity.json(DocumentContent(listOf(TypeString("hello", "text/plain", "meta")), "http://example.com/doc/1", "en-US")))
        Assertions.assertThat(response.status).isEqualTo(200)
    }


}
