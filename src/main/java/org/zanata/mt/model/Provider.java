package org.zanata.mt.model;

/**
 * Provider type
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public enum Provider {
    MS("Microsoft");

    private String displayName;

    Provider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
