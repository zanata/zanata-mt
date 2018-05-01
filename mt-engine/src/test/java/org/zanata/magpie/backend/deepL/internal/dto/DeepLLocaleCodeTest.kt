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

package org.zanata.magpie.backend.deepL.internal.dto

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.zanata.magpie.api.dto.LocaleCode

class DeepLLocaleCodeTest {

    @Test
    fun testConstructor() {
        val testLocale = LocaleCode.EN_US
        val localeCode = DeepLLocaleCode(testLocale)
        assertThat(localeCode.getLocaleCode()).isEqualTo(testLocale.getId())
    }

    @Test
    fun testConstructorString() {
        val testLocale = "en-us"
        val localeCode = DeepLLocaleCode(testLocale)
        assertThat(localeCode.localeCode).isEqualTo(testLocale)
    }

    @Test
    fun testEqualAndHashcode() {
        val zh1 = DeepLLocaleCode("zh")
        val zh2 = DeepLLocaleCode("zh")
        val ja = DeepLLocaleCode("ja")

        assertThat(zh1).isEqualTo(zh2)
        assertThat(zh1.hashCode()).isEqualTo(zh2.hashCode())
        assertThat(zh1).isNotEqualTo(ja)
        assertThat(zh2.hashCode()).isNotEqualTo(ja.hashCode())
    }
}
