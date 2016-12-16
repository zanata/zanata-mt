package org.zanata.mt;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Joiner;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This filter is for REST API requests.
 * It supports <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Access_control_CORS">
 * Cross-Origin resource sharing (CORS)</a> by adding {@code Access-Control}
 * headers to REST responses. CORS is needed for requests from different
 * domains.
 *
 * This is needed if API call is triggered from browser from different domains.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@WebFilter(filterName = "APIResponseFilter")
public class APIResponseFilter implements Filter {
    private static final String ALLOW_METHODS =
        "PUT, POST, DELETE, GET, OPTIONS";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        // Response will be different if Origin request header is different:
        servletResponse.addHeader("Vary", "Origin");

        String origin = servletRequest.getHeader("Origin");
        if (isBlank(origin)) {
            origin = "*";
        }
        servletResponse.addHeader("Access-Control-Allow-Origin", origin);

        // Allow standard HTTP methods.
        servletResponse
            .addHeader("Access-Control-Allow-Methods", ALLOW_METHODS);

        // Allow credentials in requests (eg session cookie).
        // This is potentially very dangerous, so check your Origin!
        servletResponse
            .addHeader("Access-Control-Allow-Credentials", "true");

        // Client will use these headers for the next request (assuming this is
        // a pre-flight request).
        List<String> nextRequestHeaders = Collections.list(
            servletRequest.getHeaders("Access-Control-Request-Headers"));
        Set<String> allowedHeaders = new HashSet<>(nextRequestHeaders);
        allowedHeaders.add("X-Requested-With");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Accept");

        // Allow any requested headers. Again, check your Origin!
        servletResponse.addHeader("Access-Control-Allow-Headers",
            Joiner.on(",").join(nextRequestHeaders));

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
