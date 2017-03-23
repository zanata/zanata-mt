package org.zanata.mt.servlet;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    @Before
    public void setup() {
        filter = new JavaMelodyFilter();
    }

    @Test
    @Ignore
    public void testFilterOnlyForJavaMelody() throws IOException, ServletException {
        when(request.getContextPath()).thenReturn("");
        when(request.getRequestURI()).thenReturn("/monitoring");
        filter.doFilter(request, response, chain);
        // TODO: verify for authentication
    }
}
