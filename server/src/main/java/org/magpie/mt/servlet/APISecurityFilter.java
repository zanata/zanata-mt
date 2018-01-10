package org.magpie.mt.servlet;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.magpie.mt.api.APIConstant.API_KEY;
import static org.magpie.mt.api.APIConstant.HEADER_API_KEY;
import static org.magpie.mt.api.APIConstant.HEADER_USERNAME;
import static org.magpie.mt.api.APIConstant.API_ID;

/**
 * Filter for handling auth in /api path.
 * Request header required to have a matching
 * {@link org.magpie.mt.api.APIConstant#API_ID} and {@link org.magpie.mt.api.APIConstant#API_KEY} with request header
 * {@link org.magpie.mt.api.APIConstant#HEADER_USERNAME} and {@link org.magpie.mt.api.APIConstant#HEADER_API_KEY}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@WebFilter(filterName = "APISecurityFilter", value = { "/api/*" })
public class APISecurityFilter implements Filter {

    private static final Logger LOG =
        LoggerFactory.getLogger(APISecurityFilter.class);

    /**
     * Nonnull value. Verified during startup
     * {@link org.magpie.mt.service.ZanataMTStartup#verifyCredentials
     */
    private static RestCredentials REST_CREDENTIALS;

    static {
        REST_CREDENTIALS = new RestCredentials(System.getenv(API_ID),
            System.getenv(API_KEY));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;

        RestCredentials requestCredentials = new RestCredentials(servletRequest);

        if (!REST_CREDENTIALS.equals(requestCredentials)) {
            String error = "API key authentication failed for user. " +
                    (requestCredentials.hasUsername() ? requestCredentials.username.get() : "");
            LOG.info(error);
            servletResponse
                    .sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
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
