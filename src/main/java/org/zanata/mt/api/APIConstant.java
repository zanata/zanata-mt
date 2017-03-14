package org.zanata.mt.api;

/**
 * System properties and HTTP header fields used in application
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class APIConstant {

    @SuppressWarnings("unused")
    private APIConstant() {
    }

    // System properties
    // list of url to allow access to /api. White space separated
    public static final String ORIGIN_WHITELIST = "ZANATA_MT_ORIGIN_WHITELIST";
    // API_ID to be used for API request
    public static final String API_ID = "ZANATA_MT_API_ID";
    // API Key to be used for API request
    public static final String API_KEY = "ZANATA_MT_API_KEY";
    // AZURE Subscription key for MS backend
    public static final String AZURE_KEY = "ZANATA_MT_AZURE_KEY";

    // HTTP header request authentication
    public static final String HEADER_API_KEY = "X-Auth-Token";
    public static final String HEADER_USERNAME = "X-Auth-User";
}
