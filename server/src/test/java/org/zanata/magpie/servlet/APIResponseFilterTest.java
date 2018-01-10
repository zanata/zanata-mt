package org.zanata.magpie.servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class APIResponseFilterTest {

    private APIResponseFilter filter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    @Before
    public void beforeTest() throws ServletException {
        filter = new APIResponseFilter();
        filter.init(null);
    }

    @After
    public void afterTest() {
        filter.destroy();
    }

    @Test
    public void testEmptyWhiteList() throws IOException, ServletException {
        filter.setOriginWhitelist("");

        filter.doFilter(request, response, chain);
        verifyZeroInteractions(request);
        verifyZeroInteractions(response);
    }

    @Test
    public void testEmptyOrigin() throws IOException, ServletException {
        filter.setOriginWhitelist("http://localhost");

        filter.doFilter(request, response, chain);
        verify(response).addHeader("Vary", "Origin");
        verify(request).getHeader("Origin");
        verifyNoMoreInteractions(request, response);
    }

    @Test
    public void testFilter() throws IOException, ServletException {
        filter.setOriginWhitelist("http://localhost");

        when(request.getHeader("Origin")).thenReturn("http://localhost");

        Enumeration enumList = Mockito.mock(Enumeration.class);

        when(request.getHeaders("Access-Control-Request-Headers"))
                .thenReturn(enumList);

        filter.doFilter(request, response, chain);
        verify(response, times(4)).addHeader(anyString(), anyString());
    }

    @Test
    public void testRestCredentialsEmpty() {
        APISecurityFilter.RestCredentials restCredentials =
                new APISecurityFilter.RestCredentials(null, null);
        assertThat(restCredentials.hasUsername()).isFalse();
        assertThat(restCredentials.hasApiKey()).isFalse();
    }

    @Test
    public void testRestCredentialsNotEmpty() {
        APISecurityFilter.RestCredentials restCredentials =
                new APISecurityFilter.RestCredentials("user", "api");
        assertThat(restCredentials.hasUsername()).isTrue();
        assertThat(restCredentials.hasApiKey()).isTrue();
    }

    @Test
    public void testRestCredentialsEqualsAndHashCode() {
        APISecurityFilter.RestCredentials restCredentials =
                new APISecurityFilter.RestCredentials("user", "api");

        APISecurityFilter.RestCredentials restCredentials2 =
                new APISecurityFilter.RestCredentials("user", "api");
        assertThat(restCredentials).isEqualTo(restCredentials2);
        assertThat(restCredentials.hashCode())
                .isEqualTo(restCredentials2.hashCode());
    }
}
