/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
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
package org.zanata.magpie.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.zanata.magpie.api.APIConstant.AZURE_KEY;
import static org.zanata.magpie.api.APIConstant.DEFAULT_PROVIDER;
import static org.zanata.magpie.api.APIConstant.GOOGLE_ADC;
import static org.zanata.magpie.api.APIConstant.GOOGLE_CREDENTIAL_CONTENT;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.Credentials;
import org.zanata.magpie.annotation.DefaultProvider;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.annotation.EnvVariable;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.api.APIConstant;
import org.zanata.magpie.backend.google.GoogleCredential;
import org.zanata.magpie.model.BackendID;

import com.google.common.collect.Sets;

/**
 * This service will startup in eager loading in application. It is used to
 * cache build information from build.properties
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
@Startup
public class ConfigurationService {
    private static final Logger LOG =
            LoggerFactory.getLogger(ConfigurationService.class);
    private GoogleCredential googleCredential;

    private String version;
    private String buildDate;
    private String msAPIKey;
    private boolean isDevMode;

    private String id;
    private String apiKey;
    private BackendID defaultTranslationProvider;

    @SuppressWarnings("unused")
    public ConfigurationService() {
    }

    @Inject
    public ConfigurationService(@EnvVariable(APIConstant.API_ID) String id,
            @EnvVariable(APIConstant.API_KEY) String apiKey,
            @EnvVariable(AZURE_KEY) String msAPIKey,
            @EnvVariable(GOOGLE_ADC) String googleADC,
            @EnvVariable(GOOGLE_CREDENTIAL_CONTENT) String googleADCContent,
            @EnvVariable(DEFAULT_PROVIDER) String defaultProvider) {
        this.id = id;
        this.apiKey = apiKey;
        this.msAPIKey = msAPIKey;

        this.googleCredential = GoogleCredential.from(googleADC, googleADCContent);

        defaultTranslationProvider = BackendID.fromString(defaultProvider);

        isDevMode = isBlank(msAPIKey) && !googleCredential.exists();

        Properties properties = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("build.properties")) {
            properties.load(is);
            buildDate = properties.getProperty("build.date", "Unknown");
            version = properties.getProperty("build.version", "Unknown");

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
    protected BackendID
            getDefaultTranslationProvider(@DevMode boolean isDevMode) {
        return isDevMode ? BackendID.DEV : defaultTranslationProvider;
    }

    @Produces
    @Credentials(BackendID.GOOGLE)
    protected GoogleCredential googleDefaultCredential() {
        return googleCredential;
    }

    public String getId() {
        return id;
    }

    public String getApiKey() {
        return apiKey;
    }

    @Produces
    @Credentials(BackendID.MS)
    protected String getMsAPIKey() {
        return msAPIKey;
    }

    @Produces
    @BackEndProviders
    protected Set<BackendID> availableProviders(
            @Credentials(BackendID.GOOGLE) GoogleCredential googleCredential,
            @Credentials(BackendID.MS) String msCredential,
            @DevMode boolean isDevMode) {
        Set<BackendID> providers = Sets.newHashSet();
        if (googleCredential.exists()) {
            providers.add(BackendID.GOOGLE);
        }
        if (!isBlank(msCredential)) {
            providers.add(BackendID.MS);
        }
        if (isDevMode) {
            providers.add(BackendID.DEV);
        }
        return providers;
    }

}
