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

package org.zanata.magpie.backend.deepL;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLLocaleCode;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLResp;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLRespEntry;
import org.zanata.magpie.model.AugmentedTranslation;
import org.zanata.magpie.util.DTOUtil;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DeepLBackendTest {

    private DeepLBackend deepLBackend = null;
    String key = "key";

    @Test
    public void testConstructor() {
        deepLBackend = new DeepLBackend();
    }

    @Test
    public void testMappedLocale() {
        LocaleCode from = LocaleCode.EN_US;

        deepLBackend = new DeepLBackend(key);
        BackendLocaleCode to = deepLBackend.getMappedLocale(from);
        assertThat(to.getLocaleCode()).isNotEqualTo(from.getId());

        from = LocaleCode.PT;
        to = deepLBackend.getMappedLocale(from);
        assertThat(to.getLocaleCode()).isEqualTo(from.getId());
    }

    @Test
    public void testTranslate() {
        deepLBackend = new DeepLBackend(key);
        String content = "content";
        DeepLLocaleCode srcLocale = new DeepLLocaleCode(LocaleCode.EN);
        DeepLLocaleCode transLocale = new DeepLLocaleCode(LocaleCode.DE);

        DeepLRespEntry respEntry = new DeepLRespEntry();
        respEntry.setSrcLanguage(srcLocale.getLocaleCode());
        respEntry.setTranslation("translation");
        DeepLResp resp = new DeepLResp();
        resp.setTranslations(Lists.newArrayList(respEntry));

        DeepLClient client = Mockito.mock(DeepLClient.class);
        when(client.requestTranslations(any(), eq(srcLocale), eq(transLocale),
                eq(key))).thenReturn(resp);
        deepLBackend.setClient(client);

        List<AugmentedTranslation> translations =
                deepLBackend.translate(Lists.newArrayList(content),
                        srcLocale, transLocale, MediaType.TEXT_PLAIN_TYPE,
                        Optional.empty());

        assertThat(translations.size()).isEqualTo(1);
        assertThat(translations.get(0).getPlainTranslation())
                .isEqualTo(respEntry.getTranslation());
    }
}
