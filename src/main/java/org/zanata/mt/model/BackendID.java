package org.zanata.mt.model;

/**
 * Backend type
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendID {

    public static final BackendID MS = new BackendID("MS");

    private String id;
    public BackendID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
