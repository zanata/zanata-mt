package org.zanata.mt.api;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class APIConstantTest {

    @Test
    public void testConstructor() {
        APIConstant constant = new APIConstant();
    }

    @Test
    public void testOriginWhiteList() {
        assertThat(APIConstant.ORIGIN_WHITELIST).isNotEmpty();
    }

    @Test
    public void testId() {
        assertThat(APIConstant.ID).isNotEmpty();
    }

    @Test
    public void testAPIKey() {
        assertThat(APIConstant.API_KEY).isNotEmpty();
    }

    @Test
    public void testAzureID() {
        assertThat(APIConstant.AZURE_ID).isNotEmpty();
    }

    @Test
    public void testAzureSecret() {
        assertThat(APIConstant.AZURE_SECRET).isNotEmpty();
    }

    @Test
    public void testHeaderApiKey() {
        assertThat(APIConstant.HEADER_API_KEY).isNotEmpty();
    }

    @Test
    public void testHeaderUsername() {
        assertThat(APIConstant.HEADER_USERNAME).isNotEmpty();
    }
}
