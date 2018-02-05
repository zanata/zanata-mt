package org.zanata.magpie.api.service.impl;

import org.zanata.magpie.api.dto.ApplicationInfo;
import org.zanata.magpie.api.service.InfoResource;
import org.zanata.magpie.service.ConfigurationService;
import org.zanata.magpie.service.MTStartup;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class InfoResourceImpl implements InfoResource {

    private ConfigurationService configurationService;

    @SuppressWarnings("unused")
    public InfoResourceImpl() {
    }

    @Inject
    public InfoResourceImpl(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Override
    public Response getInfo() {
        ApplicationInfo info = new ApplicationInfo(MTStartup.APPLICATION_NAME,
                configurationService.getVersion(),
                configurationService.getBuildDate(),
                configurationService.isDevMode());
        return Response.ok().entity(info).build();
    }
}
