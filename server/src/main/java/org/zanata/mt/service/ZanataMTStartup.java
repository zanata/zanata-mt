package org.zanata.mt.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.APIConstant;
import org.zanata.mt.exception.ZanataMTException;

/**
 * Startup monitor for Zanata MT.
 *
 * Insert any check needed when startup.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class ZanataMTStartup {
    private static final Logger LOG =
        LoggerFactory.getLogger(ZanataMTStartup.class);

    private ConfigurationService configurationService;

    @Inject
    public ZanataMTStartup(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void onStartUp(
        @Observes @Initialized(ApplicationScoped.class) Object init)
        throws ZanataMTException {
        LOG.info("===================================");
        LOG.info("===================================");
        LOG.info("=== Machine Translation Service ===");
        LOG.info("===================================");
        LOG.info("===================================");
        LOG.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (configurationService.isDevMode()) {
            LOG.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        verifyCredentials();
    }

    public void verifyCredentials() {
        if (StringUtils.isBlank(configurationService.getId()) ||
                StringUtils.isBlank(configurationService.getApiKey())) {
            throw new ZanataMTException(
                    "Missing credentials of " + APIConstant.API_ID + " and " +
                            APIConstant.API_KEY);
        }
    }
}
