package org.zanata.magpie.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;

import com.google.common.base.Strings;

/**
 * Backend type
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum  BackendID implements Serializable {

    // Microsoft translators service
    MS("MS"),
    // Google translation service
    GOOGLE("GOOGLE"),
    // DeepL
    DEEPL("DEEPL"),

    // DEV translators service
    DEV("DEV");

    @NotNull
    private final String id;

    BackendID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * JAX-RS will use this method to marshall between string and this enum.
     * Supports abbreviation. e.g. 'm' will map to 'MS'.
     *
     * @param value
     *            string representation of the enum constants
     * @return the enum constant
     */
    public static BackendID fromString(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return null;
        }
        if (value.toLowerCase().equals("ms")) {
            return MS;
        }
        if (value.toLowerCase().equals("google")) {
            return GOOGLE;
        }
        if (value.toLowerCase().equals("deepl")) {
            return DEEPL;
        }
        if (value.equalsIgnoreCase("dev")) {
            return DEV;
        }
        throw new BadRequestException(
                "can not parse [" + value + "] to a BackendID");
    }

    public static BackendID fromStringWithDefault(String value, BackendID defaultProvider) {
        BackendID backendID = fromString(value);
        return backendID == null ? defaultProvider : backendID;
    }
}
