package org.zanata.magpie.servlet;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.zanata.magpie.service.MTStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.zanata.magpie.api.APIConstant.API_KEY;
import static org.zanata.magpie.api.APIConstant.HEADER_API_KEY;
import static org.zanata.magpie.api.APIConstant.HEADER_USERNAME;
import static org.zanata.magpie.api.APIConstant.API_ID;
import static org.zanata.magpie.servlet.APISecurityFilter.API_PATH;

/**
 * Filter for handling auth in /api path.
 * Request header required to have a matching
 * {@link org.zanata.magpie.api.APIConstant#API_ID} and {@link org.zanata.magpie.api.APIConstant#API_KEY} with request header
 * {@link org.zanata.magpie.api.APIConstant#HEADER_USERNAME} and {@link org.zanata.magpie.api.APIConstant#HEADER_API_KEY}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@WebFilter(filterName = "APISecurityFilter", value = { API_PATH + "*" })
public class APISecurityFilter implements Filter {
    public static final String API_PATH = "/api/";

    private static final Logger LOG =
        LoggerFactory.getLogger(APISecurityFilter.class);

    /**
     * Nonnull value. Verified during startup
     * {@link MTStartup#verifyCredentials
     */
    private static RestCredentials REST_CREDENTIALS;

    private static ImmutableList<String> PUBLIC_API;

    static {
        REST_CREDENTIALS = new RestCredentials(System.getenv(API_ID),
            System.getenv(API_KEY));
        PUBLIC_API = ImmutableList.of(API_PATH + "info");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        if (!isPublicAPI(servletRequest.getRequestURI())) {
            HttpServletResponse servletResponse =
                    (HttpServletResponse) response;

            RestCredentials requestCredentials =
                    new RestCredentials(servletRequest);

            if (!REST_CREDENTIALS.equals(requestCredentials)) {
                String error = "API key authentication failed for user. " +
                        (requestCredentials.hasUsername() ?
                                requestCredentials.username.get() : "");
                LOG.info(error);
                servletResponse
                        .sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private boolean isPublicAPI(String uri) {
        return PUBLIC_API.contains(uri);
    }

    protected static class RestCredentials {
        private final @NotNull Optional<String> username;
        private final @NotNull Optional<String> apiKey;

        RestCredentials(String username, String apiKey) {
            this.username = Optional.ofNullable(username);
            this.apiKey = Optional.ofNullable(apiKey);
        }

        RestCredentials(HttpServletRequest request) {
            this.username = Optional.ofNullable(request.getHeader(HEADER_USERNAME));
            this.apiKey = Optional.ofNullable(request.getHeader(HEADER_API_KEY));
        }

        boolean hasUsername() {
            return username.isPresent();
        }

        boolean hasApiKey() {
            return apiKey.isPresent();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RestCredentials)) return false;

            RestCredentials that = (RestCredentials) o;

            return username.equals(that.username) && apiKey.equals(that.apiKey);
        }

        @Override
        public int hashCode() {
            int result = username.hashCode();
            result = 31 * result + apiKey.hashCode();
            return result;
        }
    }

    @VisibleForTesting
    static void setAPIIdAndKey(String id, String key) {
        REST_CREDENTIALS = new RestCredentials(id, key);
    }
}
