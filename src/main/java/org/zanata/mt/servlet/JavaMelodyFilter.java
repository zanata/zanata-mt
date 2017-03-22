package org.zanata.mt.servlet;

import net.bull.javamelody.MonitoringFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Overrides JavaMelody MonitoringFilter so that only admin users can
 * access the JavaMelody console.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class JavaMelodyFilter extends MonitoringFilter {
    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getRequestURI().equals(getMonitoringUrl(httpRequest))) {
            // TODO: add authentication for user and password
            super.doFilter(request, response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }
}
