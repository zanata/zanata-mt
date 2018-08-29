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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.ApplicationInfo;
import org.zanata.magpie.api.service.impl.InfoResourceImpl;
import org.zanata.magpie.service.ConfigurationService;
import org.zanata.magpie.service.MTStartup;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InfoResourceImplTest {
    private InfoResourceImpl infoResource;

    @Mock
    private ConfigurationService configurationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructor() {
        infoResource = new InfoResourceImpl();
    }

    @Test
    public void testGetInfo() {
        String version = "version";
        String buildDate = "buildDate";
        boolean devMode = true;
        when(configurationService.getVersion()).thenReturn(version);
        when(configurationService.getBuildDate()).thenReturn(buildDate);
        when(configurationService.isDevMode()).thenReturn(devMode);

        infoResource = new InfoResourceImpl(configurationService);
        Response response = infoResource.getInfo();
        assertThat(response.getStatus())
            .isEqualTo(Response.Status.OK.getStatusCode());
        ApplicationInfo info = (ApplicationInfo)response.getEntity();
        assertThat(info.getName()).isEqualTo(MTStartup.APPLICATION_NAME);
        assertThat(info.getVersion()).isEqualTo(version);
        assertThat(info.getBuildDate()).isEqualTo(buildDate);
        assertThat(info.isDevMode()).isEqualTo(devMode);
    }
}
