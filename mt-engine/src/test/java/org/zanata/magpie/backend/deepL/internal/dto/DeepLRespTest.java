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

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeepLRespTest {

    @Test
    public void testTranslations() {
        DeepLRespEntry resp1 = new DeepLRespEntry();
        resp1.setTranslation("translations1");
        resp1.setSrcLanguage("en");

        DeepLRespEntry resp2 = new DeepLRespEntry();
        resp2.setTranslation("translations2");
        resp2.setSrcLanguage("en");

        DeepLResp resp = new DeepLResp();
        resp.setTranslations(Lists.newArrayList(resp1, resp2));
        assertThat(resp.getTranslations()).hasSize(2).contains(resp1, resp2);
    }

}
