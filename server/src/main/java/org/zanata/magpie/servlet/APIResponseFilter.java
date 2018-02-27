package org.zanata.magpie.servlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Priorities;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.zanata.magpie.api.APIConstant.ORIGIN_WHITELIST;
import static org.zanata.magpie.servlet.APISecurityFilter.API_PATH;

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
@WebFilter(filterName = "APIResponseFilter", value = { API_PATH + "*" })
@Priority(Priorities.HEADER_DECORATOR)
public class APIResponseFilter implements Filter {
    private static final String ALLOW_METHODS =
        "PUT, POST, DELETE, GET, OPTIONS";

    private static final Logger LOG =
        LoggerFactory.getLogger(APIResponseFilter.class);

    private static ImmutableList<String> originWhitelist;

    static {
        String whitelist = System.getenv(ORIGIN_WHITELIST);
        originWhitelist = StringUtils.isBlank(whitelist) ? ImmutableList.of() :
                ImmutableList.copyOf(whitelist.split(" +"));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {
        if (originWhitelist.isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletResponse servletResponse = (HttpServletResponse) response;
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        // Response will be different if Origin request header is different:
        servletResponse.addHeader("Vary", "Origin");

        // Allow the specified Origin, but only if it is whitelisted.
        String origin = servletRequest.getHeader("Origin");
        if (!StringUtils.isBlank(origin) && originWhitelist.contains(origin)) {
            try {
                URIBuilder ub = new URIBuilder(origin);
                servletResponse
                    .addHeader("Access-Control-Allow-Origin", ub.toString());
            } catch (URISyntaxException e) {
                LOG.error("Unable to include `Access-Control-Allow-Origin` in header");
            }

            // Allow standard HTTP methods.
            servletResponse
                .addHeader("Access-Control-Allow-Methods", ALLOW_METHODS);

            // Allow credentials in requests (eg session cookie).
            // This is potentially very dangerous, so check your Origin!
            servletResponse
                .addHeader("Access-Control-Allow-Credentials", "true");

            // Client will use these headers for the next request (assuming this is
            // a pre-flight request).

            Enumeration<String> enumList =
                    servletRequest.getHeaders("Access-Control-Request-Headers");
            if (enumList.hasMoreElements()) {
                List<String> nextRequestHeaders = Collections.list(enumList);
                // Allow any requested headers. Again, check your Origin!
                servletResponse.addHeader("Access-Control-Allow-Headers",
                        Joiner.on(",").join(nextRequestHeaders));
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    @VisibleForTesting
    protected void setOriginWhitelist(@Nullable String whitelist) {
        originWhitelist = StringUtils.isBlank(whitelist) ? ImmutableList.of() :
                ImmutableList.copyOf(whitelist.split(" +"));
    }
}
