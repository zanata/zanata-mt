package org.zanata.mt.servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.zanata.mt.servlet.APISecurityFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.mt.api.APIConstant.HEADER_API_KEY;
import static org.zanata.mt.api.APIConstant.HEADER_USERNAME;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class APISecurityFilterTest {

    private APISecurityFilter filter;

    private String id = "id";
    private String key = "key";

    @Before
    public void beforeTest() throws ServletException {
        filter = new APISecurityFilter();
        filter.setAPIIdAndKey(id, key);
        filter.init(null);
    }

    @After
    public void afterTest() throws ServletException {
        filter.destroy();
    }

    @Test
    public void testFilterNotMatchingCredentials()
            throws IOException, ServletException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        filter.doFilter(request, response, chain);
        verifyZeroInteractions(chain);
    }

    @Test
    public void testFilterMatchingCredentials()
            throws IOException, ServletException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        FilterChain chain = Mockito.mock(FilterChain.class);

        when(request.getHeader(HEADER_USERNAME)).thenReturn(id);
        when(request.getHeader(HEADER_API_KEY)).thenReturn(key);

        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
