package org.zanata.magpie.backend.google.internal.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GoogleLocaleCodeTest {

    @Test
    fun testEqualAndHashcode() {
        val zh1 = GoogleLocaleCode("zh")
        val zh2 = GoogleLocaleCode("zh")
        val ja = GoogleLocaleCode("ja")

        assertThat(zh1).isEqualTo(zh2)
        assertThat(zh1.hashCode()).isEqualTo(zh2.hashCode())
        assertThat(zh1).isNotEqualTo(ja)
        assertThat(zh2.hashCode()).isNotEqualTo(ja.hashCode())
    }
}
