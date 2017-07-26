package org.zanata.mt.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;

/**
 * Backend type
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendID implements Serializable {

    // Microsoft translators service
    public static final BackendID MS = new BackendID("MS");

    // Google translation service
    public static final BackendID GOOGLE = new BackendID("GOOGLE");

    // DEV translators service
    public static final BackendID DEV = new BackendID("DEV");

    @NotNull
    private final String id;

    public BackendID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BackendID)) return false;

        BackendID backendID = (BackendID) o;

        return id.equals(backendID.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static BackendID fromValue(String backendId) {
        if (Strings.isNullOrEmpty(backendId)) {
            return null;
        }
        if (backendId.toLowerCase().startsWith("m")) {
            return MS;
        }
        if (backendId.toLowerCase().startsWith("g")) {
            return GOOGLE;
        }
        if (backendId.equalsIgnoreCase("dev")) {
            return DEV;
        }
        throw new IllegalArgumentException(
                "can not parse [" + backendId + "] to a translation provider");
    }
}
