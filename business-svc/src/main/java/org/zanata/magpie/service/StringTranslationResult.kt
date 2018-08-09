package org.zanata.magpie.service

import org.zanata.magpie.api.dto.APIResponse

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
data class StringTranslationResult(val translation: String, val warnings: List<APIResponse>)
