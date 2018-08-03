package org.zanata.magpie.servlet;

import com.google.common.base.Joiner;
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

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
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
    public void testEmptyWhiteList() throws Exception {
        filter.setOriginWhitelist("");

        filter.doFilter(request, response, chain);
        verifyZeroInteractions(request);
        verifyZeroInteractions(response);
    }

    @Test
    public void testEmptyRequestOrigin() throws Exception {
        filter.setOriginWhitelist("http://localhost");

        filter.doFilter(request, response, chain);
        verify(response).addHeader("Vary", "Origin");
        verify(request).getHeader("Origin");
        verifyNoMoreInteractions(request, response);
    }

    @Test
    public void testWithRequestOriginFilter() throws Exception {
        filter.setOriginWhitelist("http://localhost");

        when(request.getHeader("Origin")).thenReturn("http://localhost");

        Enumeration enumList = Mockito.mock(Enumeration.class);

        when(request.getHeaders("Access-Control-Request-Headers"))
            .thenReturn(enumList);

        filter.doFilter(request, response, chain);
        verify(response, times(4)).addHeader(anyString(), anyString());
    }


    @Test
    public void testWithAccessRequestHeaders() throws Exception {
        filter.setOriginWhitelist("http://localhost");
        when(request.getHeader("Origin")).thenReturn("http://localhost");

        Set<String> enumList = new HashSet<>();
        enumList.add("request-header1");
        enumList.add("request-header2");

        when(request.getHeaders("Access-Control-Request-Headers"))
                .thenReturn(Collections.enumeration(enumList));

        filter.doFilter(request, response, chain);
        verify(response).addHeader("Vary", "Origin");
        verify(response).addHeader("Access-Control-Allow-Headers",
                Joiner.on(",").join(enumList));
        verify(request).getHeader("Origin");
    }
}
