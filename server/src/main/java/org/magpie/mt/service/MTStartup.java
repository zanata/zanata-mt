package org.magpie.mt.service;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.magpie.mt.annotation.BackEndProviders;
import org.magpie.mt.annotation.DevMode;
import org.magpie.mt.api.APIConstant;
import org.magpie.mt.exception.MTException;
import org.magpie.mt.model.BackendID;
import org.magpie.mt.servlet.APISecurityFilter;

/**
 * Startup monitor for MT.
 *
 * Insert any check needed when startup.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MTStartup {
    private static final Logger LOG =
        LoggerFactory.getLogger(MTStartup.class);

    private ConfigurationService configurationService;

    @Inject
    public MTStartup(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void onStartUp(
        @Observes @Initialized(ApplicationScoped.class) Object init,
            @DevMode boolean isDevMode, @BackEndProviders
            Set<BackendID> availableProviders)
        throws MTException {
        LOG.info("===================================");
        LOG.info("===================================");
        LOG.info("=== Machine Translation Service ===");
        LOG.info("===================================");
        LOG.info("===================================");
        LOG.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (isDevMode) {
            LOG.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        LOG.info("Available backend providers: {}", availableProviders);
        verifyCredentials();
    }

    /**
     * This method validates the environment is set up properly with basic
     * authentication.
     * @see APISecurityFilter
     */
    public void verifyCredentials() {
        if (StringUtils.isBlank(configurationService.getId()) ||
                StringUtils.isBlank(configurationService.getApiKey())) {
            throw new MTException(
                    "Missing credentials of " + APIConstant.API_ID + " and " +
                            APIConstant.API_KEY);
        }
    }
}
