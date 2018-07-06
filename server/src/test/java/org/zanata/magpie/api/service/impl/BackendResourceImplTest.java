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

package org.zanata.magpie.api.service.impl;

import java.io.File;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.service.BackendResource;
import org.zanata.magpie.api.service.impl.BackendResourceImpl;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendResourceImplTest {

    private BackendResource backendResource;
    @Mock private InputStream inputStream;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        backendResource = new BackendResourceImpl() {
            @Override
            protected InputStream getResourceAsStream(String imageResource) {
                return inputStream;
            }
        };
    }

    @Test
    public void testGetAttributionNullId() {
        Response response = backendResource.getAttribution(null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetAttributionMSLowerCase() {
        String id = "ms";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getHeaders()).isNotEmpty()
                .containsKeys("Content-Disposition");
        assertThat(
                (String) response.getHeaders().getFirst("Content-Disposition"))
                .contains(new File(BackendResource.MS_ATTRIBUTION_IMAGE).getName());
    }

    @Test
    public void testGetAttributionDEVLowerCase() {
        String id = "dev";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getHeaders()).isNotEmpty()
                .containsKeys("Content-Disposition");
        assertThat(
                (String) response.getHeaders().getFirst("Content-Disposition"))
                .contains(new File(BackendResource.DEV_ATTRIBUTION_IMAGE).getName());
    }

    @Test
    public void testGetAttributionInvalidId() {
        String id = "blah";
        Response response = backendResource.getAttribution(id);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void returnNotFoundIfGetAttributionCanNotFindImage() {
        backendResource = new BackendResourceImpl() {
            @Override
            protected InputStream getResourceAsStream(String imageResource) {
                return null;
            }
        };
        Response response = backendResource.getAttribution("google");
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
