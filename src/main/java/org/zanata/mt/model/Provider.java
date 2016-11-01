package org.zanata.mt.model;

/**
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

    public static Provider getFromDisplayName(String displayName) {
        for (Provider provider : Provider.values()) {
            if (provider.getDisplayName().equals(displayName)) {
                return provider;
            }
        }
        throw new IllegalArgumentException(
            "cannot find provider with displayName: " + displayName);
    }
}
