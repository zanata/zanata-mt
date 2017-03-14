package org.zanata.mt.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.annotation.EnvVariable;
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

    private String version;
    private String buildDate;

    private final String id;
    private final String apiKey;

    @Inject
    public ZanataMTStartup(@EnvVariable(APIConstant.API_ID) String id,
        @EnvVariable(APIConstant.API_KEY) String apiKey) {
        this.id = id;
        this.apiKey = apiKey;
    }

    public void onStartUp(
        @Observes @Initialized(ApplicationScoped.class) Object init)
        throws ZanataMTException {
        LOG.info("============================================");
        LOG.info("============================================");
        LOG.info("=====Zanata Machine Translation Service=====");
        LOG.info("============================================");
        LOG.info("============================================");
        readBuildInfo();
        LOG.info("Build info: version-" + version + " date-" + buildDate);
        verifyCredentials();
    }

    public void readBuildInfo() {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("build.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            buildDate = properties.getProperty("build.date", "Unknown");
            version = properties.getProperty("build.version", "Unknown");
        } catch (IOException e) {
            LOG.warn("Cannot load build info");
        }
    }

    public void verifyCredentials() {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(apiKey)) {
            throw new ZanataMTException(
                "Missing credentials of " + APIConstant.API_ID + " and " + APIConstant.API_KEY);
        }
    }
}
