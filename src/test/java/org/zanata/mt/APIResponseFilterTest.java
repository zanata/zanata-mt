package org.zanata.mt;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Enumeration;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.zanata.mt.api.APIConstant.ORIGIN_WHITELIST;

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

    @Test
    public void testEmptyWhiteList() throws IOException, ServletException {
        System.setProperty(ORIGIN_WHITELIST, "");
        filter = new APIResponseFilter();

        filter.doFilter(request, response, chain);
        verifyZeroInteractions(request);
        verifyZeroInteractions(response);
    }

    @Test
    public void testEmptyOrigin() throws IOException, ServletException {
        System.setProperty(ORIGIN_WHITELIST, "http://localhost");
        filter = new APIResponseFilter();

        filter.doFilter(request, response, chain);
        verify(response).addHeader("Vary", "Origin");
        verify(request).getHeader("Origin");
        verifyNoMoreInteractions(request, response);
    }

    @Test
    public void testFilter() throws IOException, ServletException {
        System.setProperty(ORIGIN_WHITELIST, "http://localhost");
        filter = new APIResponseFilter();
        when(request.getHeader("Origin")).thenReturn("http://localhost");

        Enumeration enumList = Mockito.mock(Enumeration.class);

        when(request.getHeaders("Access-Control-Request-Headers"))
                .thenReturn(enumList);

        filter.doFilter(request, response, chain);
        verify(response, times(4)).addHeader(anyString(), anyString());
    }

}
