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

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.backend.BackendLocaleCode;
import org.zanata.magpie.backend.deepL.internal.dto.DeepLResp;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.util.DTOUtil;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;


/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DeepLClient {
    private static final Logger LOG =
            LoggerFactory.getLogger(DeepLClient.class);
    protected final static String TRANSLATIONS_BASE_URL =
            "https://api.deepl.com/v1/translate";

    public DeepLResp requestTranslations(List<String> contents,
            BackendLocaleCode srcLocale, BackendLocaleCode targetLocale,
            String authKey) {
        try {
            ResteasyWebTarget webTarget =
                    getWebTarget(contents, srcLocale, targetLocale, authKey);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Source sending:" + contents);
            }

            Response response = webTarget.request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(null));

            if (response.getStatusInfo() != Response.Status.OK) {
                throw new MTException(
                        "Error from DeepL API:"
                                + response.getStatusInfo().getReasonPhrase());
            }
            String json = response.readEntity(String.class);
            LOG.info("Translation from DeepL:" + json);
            return DTOUtil.fromJSONToObject(json, DeepLResp.class);
        } catch (IOException e) {
            throw new MTException("Error from DeepL API", e);
        }
    }

    protected ResteasyWebTarget getWebTarget(List<String> contents,
            BackendLocaleCode srcLocale, BackendLocaleCode targetLocale,
            String authKey) {
        MultivaluedMap<String, Object> params = new MultivaluedHashMap<>();

        params.addAll("text", contents);
        params.add("source_lang", srcLocale.getLocaleCode());
        params.add("target_lang", targetLocale.getLocaleCode());
        params.add("auth_key", authKey);

        ResteasyWebTarget webTarget = new ResteasyClientBuilder().build()
                .target(TRANSLATIONS_BASE_URL).queryParams(params);

        return webTarget;
    }
}
