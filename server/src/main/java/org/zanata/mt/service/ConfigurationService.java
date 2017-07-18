package org.zanata.mt.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.annotation.Credentials;
import org.zanata.mt.annotation.DefaultProvider;
import org.zanata.mt.annotation.DevMode;
import org.zanata.mt.annotation.EnvVariable;
import org.zanata.mt.api.APIConstant;
import org.zanata.mt.api.dto.TranslationProvider;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.zanata.mt.api.APIConstant.AZURE_KEY;
import static org.zanata.mt.api.APIConstant.DEFAULT_PROVIDER;
import static org.zanata.mt.api.APIConstant.GOOGLE_ADC;

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
    private String msAPIKey;
    private File googleADC;
    private boolean isDevMode;

    private String id;
    private String apiKey;
    private TranslationProvider defaultTranslationProvider;

    @SuppressWarnings("unused")
    public ConfigurationService() {
    }

    @Inject
    public ConfigurationService(@EnvVariable(APIConstant.API_ID) String id,
            @EnvVariable(APIConstant.API_KEY) String apiKey,
            @EnvVariable(AZURE_KEY) String msAPIKey,
            @EnvVariable(GOOGLE_ADC) String googleADC,
            @EnvVariable(DEFAULT_PROVIDER) String defaultProvider) {
        this.id = id;
        this.apiKey = apiKey;
        this.msAPIKey = msAPIKey;
        this.googleADC = new File(googleADC);
        defaultTranslationProvider =
                TranslationProvider.fromString(defaultProvider);

        InputStream is = getClass().getClassLoader()
                .getResourceAsStream("build.properties");
        Properties properties = new Properties();
        try {
            properties.load(is);
            buildDate = properties.getProperty("build.date", "Unknown");
            version = properties.getProperty("build.version", "Unknown");
            isDevMode = StringUtils.isBlank(msAPIKey) &&
                    StringUtils.isBlank(googleADC);
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

    @Produces
    @DevMode
    public boolean isDevMode() {
        return isDevMode;
    }

    @Produces
    @DefaultProvider
    protected TranslationProvider getDefaultTranslationProvider(@DevMode boolean isDevMode) {
        return isDevMode ? TranslationProvider.Dev : defaultTranslationProvider;
    }

    @Produces
    @Credentials(TranslationProvider.Google)
    protected File googleDefaultCredentialFile() {
        return googleADC;
    }

    public String getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Produces
    @Credentials(TranslationProvider.Microsoft)
    protected String getMsAPIKey() {
        return msAPIKey;
    }

}
