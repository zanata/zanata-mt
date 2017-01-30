package org.zanata.mt.model;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

/**
 * Backend type
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendID implements Serializable {

    // Microsoft translators service
    public static final BackendID MS = new BackendID("MS");

    @NotNull
    private final String id;

    public BackendID(String id) {
        this.id = id;
    }

    public String getId() {
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
}
