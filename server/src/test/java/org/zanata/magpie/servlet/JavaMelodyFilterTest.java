package org.zanata.magpie.servlet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class JavaMelodyFilterTest {
    private JavaMelodyFilter filter;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;
    @Mock
    private ServletContext servletContext;
    @Mock
    private FilterConfig filterConfig;

    @Before
    public void setup() {
        filter = new JavaMelodyFilter();
    }

    @Test
    @Ignore
    public void testFilterOnlyForJavaMelody() throws IOException, ServletException {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/monitoring");
        when(filterConfig.getServletContext()).thenReturn(servletContext);

        filter.init(filterConfig);
        filter.doFilter(request, response, chain);
        verifyNoMoreInteractions(chain);
    }

    @Test
    public void testFilterNotJavaMelodyRequest() throws IOException, ServletException {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/not monitoring");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }
}
