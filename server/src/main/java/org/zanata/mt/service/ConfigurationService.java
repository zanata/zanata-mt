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
package org.zanata.mt.service;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.zanata.mt.api.APIConstant.AZURE_KEY;
import static org.zanata.mt.api.APIConstant.DEFAULT_PROVIDER;
import static org.zanata.mt.api.APIConstant.GOOGLE_ADC;
import static org.zanata.mt.api.APIConstant.GOOGLE_CREDENTIAL_CONTENT;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.annotation.Credentials;
import org.zanata.mt.annotation.DefaultProvider;
import org.zanata.mt.annotation.DevMode;
import org.zanata.mt.annotation.EnvVariable;
import org.zanata.mt.api.APIConstant;
import org.zanata.mt.model.BackendID;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

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

    private String version;
    private String buildDate;
    private String msAPIKey;
    private File googleADCFile;
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
        this.googleADCFile = new File(googleADC);

        defaultTranslationProvider = BackendID.fromString(defaultProvider);

        if (!isBlank(googleADCContent)) {
            writeGoogleADCFile(googleADCFile, googleADCContent);
        }

        isDevMode = isBlank(msAPIKey) && hasNoGoogleApplicationCredential();

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

    private static void writeGoogleADCFile(File googleADCFile,
            String googleADCContent) {
        Preconditions.checkArgument(
                googleADCFile.exists() && googleADCFile.isFile()
                        && googleADCFile.canRead(),
                "%s is not a valid file path", googleADCFile);

        googleADCFile.getParentFile().mkdirs();
        try {
            Files.write(googleADCContent, googleADCFile, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to write Google Application Default Credentials file to "
                            + googleADCFile);
        }
    }

    private boolean hasNoGoogleApplicationCredential() {
        try {
            return Files.readLines(googleADCFile, Charsets.UTF_8).isEmpty();
        } catch (IOException e) {
            throw new RuntimeException(
                    "can not read Google Application Default Credentials file");
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
    protected File googleDefaultCredentialFile() {
        return googleADCFile;
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

}
