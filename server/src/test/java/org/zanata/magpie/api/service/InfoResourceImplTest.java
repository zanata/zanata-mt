package org.zanata.magpie.api.service;

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
