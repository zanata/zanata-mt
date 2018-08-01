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
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLLocaleCode;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLResp;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLRespEntry;
import org.zanata.magpie.util.DTOUtil;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeepLClientTest {

    @Test
    public void testGetWebTarget() {
        DeepLClient client = new DeepLClient();
        List<String> contents = Lists.newArrayList("testing1", "testing2");
        DeepLLocaleCode fromLocale = new DeepLLocaleCode("en");
        DeepLLocaleCode toLocale = new DeepLLocaleCode("de");
        String key = "key";

        ResteasyWebTarget target =
                client.getWebTarget(contents, fromLocale, toLocale, key);
        assertThat(target.getUri().toString())
                .startsWith(DeepLClient.TRANSLATIONS_BASE_URL);
        assertThat(target.getUri().getQuery())
                .contains(fromLocale.getLocaleCode())
                .contains(toLocale.getLocaleCode()).contains(key)
                .contains(contents);
    }

    @Test
    public void testRequestTranslations() {
        DeepLClient client = spy(new DeepLClient());
        List<String> contents = Lists.newArrayList("testing1", "testing2");
        DeepLLocaleCode fromLocale = new DeepLLocaleCode("en");
        DeepLLocaleCode toLocale = new DeepLLocaleCode("de");
        String key = "key";
        String json = "{}";

        Response response = Mockito.mock(Response.class);
        when(response.getStatusInfo()).thenReturn(Response.Status.OK);
        when(response.readEntity(String.class)).thenReturn(json);

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        ResteasyWebTarget target = Mockito.mock(ResteasyWebTarget.class);
        when(target.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        when(builder.post(any())).thenReturn(response);

        when(client.getWebTarget(contents, fromLocale, toLocale, key))
                .thenReturn(target);

        DeepLResp result =
                client.requestTranslations(contents, fromLocale, toLocale, key);
        System.out.println(result);
    }

    @Test
    public void testJson() {
        DeepLRespEntry entry1 = new DeepLRespEntry();
        entry1.setTranslation("trans1");
        entry1.setSrcLanguage("de");

        DeepLRespEntry entry2 = new DeepLRespEntry();
        entry1.setTranslation("trans2");
        entry1.setSrcLanguage("fr");

        DeepLResp resp = new DeepLResp();
        resp.setTranslations(Lists.newArrayList(entry1, entry2));
        System.out.println(DTOUtil.toJSON(resp));
    }

    @Test
    public void test() throws IOException {
        String json = "{\"translations\":[{\"detected_source_language\":\"EN\",\"text\":\"(Testen123)\"}]}";
        DTOUtil.fromJSONToObject(json, DeepLResp.class);
    }
}
