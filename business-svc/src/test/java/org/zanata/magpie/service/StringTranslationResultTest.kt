/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.magpie.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.zanata.magpie.api.dto.APIResponse
import java.util.ArrayList

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */

class StringTranslationResultTest {
    @Test
    fun  testConstructor() {
        val response = ArrayList<APIResponse>()
        val (translation, warnings) = StringTranslationResult("string", response)
        assertThat(translation).isEqualTo("string")
        assertThat<APIResponse>(warnings).isEqualTo(response)
    }

    @Test
    fun testHashCodeEquals() {
        val response = ArrayList<APIResponse>()
        val result = StringTranslationResult("string", response)
        var result2 = StringTranslationResult("string", response)
        assertThat(result.hashCode()).isEqualTo(result2.hashCode())
        assertThat(result == result2).isTrue()

        result2 = StringTranslationResult("string2", response)
        assertThat(result.hashCode()).isNotEqualTo(result2.hashCode())
        assertThat(result == result2).isFalse()
    }
}