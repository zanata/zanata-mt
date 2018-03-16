package org.zanata.magpie.service;

import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.api.APIConstant;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.servlet.APISecurityFilter;

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

    public static final String APPLICATION_NAME = "Magpie service (Machine Translation)";

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
        LOG.info("==========================================");
        LOG.info("==========================================");
        LOG.info("== " + APPLICATION_NAME + " ==");
        LOG.info("==========================================");
        LOG.info("==========================================");
        LOG.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (isDevMode) {
            LOG.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        LOG.info("Available backend providers: {}", availableProviders);
        LOG.info("Default backend provider: {}",
                configurationService.getDefaultTranslationProvider(isDevMode));
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
