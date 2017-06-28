package org.zanata.mt.service;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.annotation.EnvVariable;
import org.zanata.mt.api.APIConstant;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.zanata.mt.api.APIConstant.AZURE_KEY;

/**
 * This service will startup in eager loading in application.
 * It is used to cache build information from build.properties
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
@Startup
public class ConfigurationService {
    private static final Logger LOG =
            LoggerFactory.getLogger(ConfigurationService.class);

    private String version;
    private String buildDate;
    private String clientSubscriptionKey;
    private boolean isDevMode;

    private String id;
    private String apiKey;

    @SuppressWarnings("unused")
    public ConfigurationService() {
    }

    @Inject
    public ConfigurationService(@EnvVariable(APIConstant.API_ID) String id,
            @EnvVariable(APIConstant.API_KEY) String apiKey,
            @EnvVariable(AZURE_KEY) String clientSubscriptionKey) {
        this.id = id;
        this.apiKey = apiKey;
        this.clientSubscriptionKey = clientSubscriptionKey;

        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("build.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            buildDate = properties.getProperty("build.date", "Unknown");
            version = properties.getProperty("build.version", "Unknown");
            isDevMode = StringUtils.isBlank(clientSubscriptionKey);
        } catch (IOException e) {
            LOG.warn("Cannot load build info");
        }
    }

    public String getVersion() {
        return version;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public boolean isDevMode() {
        return isDevMode;
    }

    public String getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getClientSubscriptionKey() {
        return clientSubscriptionKey;
    }
}
