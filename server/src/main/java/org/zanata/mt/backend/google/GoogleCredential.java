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
package org.zanata.mt.backend.google;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Encapsulates Google credential information.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GoogleCredential {
    private static final Logger log =
            LoggerFactory.getLogger(GoogleCredential.class);
    public static final GoogleCredential ABSENT = new GoogleCredential(null);
    private final File credentialsFile;

    @VisibleForTesting
    public GoogleCredential(File credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    public static GoogleCredential from(String credentialsFile,
            String credentialFileContent) {
        if (isBlank(credentialsFile)) {
            return ABSENT;
        }
        File googleADCFile = new File(credentialsFile);
        if (!isBlank(credentialFileContent)) {
            writeGoogleADCFile(googleADCFile, credentialFileContent);
        }
        boolean validCredentialFile = googleADCFile.exists()
                && isValidGoogleCredentialFile(googleADCFile);
        if (!validCredentialFile) {
            return ABSENT;
        }
        return new GoogleCredential(googleADCFile);
    }

    public File getCredentialsFile() {
        return credentialsFile;
    }

    public boolean exists() {
        return this != ABSENT;
    }

    private static void writeGoogleADCFile(File googleADCFile,
            String googleADCContent) {
        Preconditions.checkArgument(
                googleADCFile.exists() && googleADCFile.isFile()
                        && googleADCFile.canWrite(),
                "%s is not a valid file path", googleADCFile);

        boolean mkdirs = googleADCFile.getParentFile().mkdirs();
        log.info("{} parent dir created: {}", googleADCFile, mkdirs);
        try {

            Files.write(googleADCFile.toPath(),
                    Lists.newArrayList(googleADCContent));
        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to write Google Application Default Credentials file to "
                            + googleADCFile);
        }
    }

    private static boolean
            isValidGoogleCredentialFile(@Nonnull File googleADCFile) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GoogleCredentialBasic value =
                    objectMapper.readerFor(GoogleCredentialBasic.class)
                            .readValue(googleADCFile);
            return value.isValid();
        } catch (Exception e) {
            log.error(
                    "unable to read Google Application Default Credentials file",
                    e);
            return false;
        }
    }

    /**
     * We try to unmarshall the Google Default Credential file and check if it's
     * a valid one.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GoogleCredentialBasic {
        // we only check the existence of these two fields
        private String authUri;
        private String type;

        @JsonProperty("auth_uri")
        public String getAuthUri() {
            return authUri;
        }

        public String getType() {
            return type;
        }

        boolean isValid() {
            return Objects.equals(getAuthUri(),
                    "https://accounts.google.com/o/oauth2/auth")
                    && Objects.equals(getType(), "service_account");
        }
    }
}
