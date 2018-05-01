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

package org.zanata.magpie.backend.deepL.internal.dto;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeepLRespEntryTest {

    @Test
    public void testConstructor() {
        DeepLRespEntry resp = new DeepLRespEntry();
    }

    @Test
    public void testSrcLanguage() {
        DeepLRespEntry resp = new DeepLRespEntry();
        resp.setSrcLanguage("en");
        assertThat(resp.getSrcLanguage()).isEqualTo("en");
    }

    @Test
    public void testTranslations() {
        DeepLRespEntry resp = new DeepLRespEntry();
        resp.setTranslation("translations");
        assertThat(resp.getTranslation()).isEqualTo("translations");
    }

    @Test
    public void testEqualAndHashCode() {
        DeepLRespEntry resp1 = new DeepLRespEntry();
        resp1.setTranslation("translations");
        resp1.setSrcLanguage("en");

        DeepLRespEntry resp2 = new DeepLRespEntry();
        resp2.setTranslation("translations");
        resp2.setSrcLanguage("en");
        assertThat(resp1.equals(resp2)).isTrue();
        assertThat(resp1.hashCode()).isEqualTo(resp2.hashCode());

        resp2.setTranslation("translations1");
        assertThat(resp1.equals(resp2)).isFalse();
        assertThat(resp1.hashCode()).isNotEqualTo(resp2.hashCode());

        resp2.setTranslation("translations");
        resp2.setSrcLanguage("en_us");
        assertThat(resp1.equals(resp2)).isFalse();
        assertThat(resp1.hashCode()).isNotEqualTo(resp2.hashCode());
    }

}
