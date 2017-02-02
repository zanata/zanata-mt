package org.zanata.mt;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.zanata.mt.api.APIConstant.API_KEY;
import static org.zanata.mt.api.APIConstant.HEADER_API_KEY;
import static org.zanata.mt.api.APIConstant.HEADER_USERNAME;
import static org.zanata.mt.api.APIConstant.ID;

/**
 * Filter for handling auth in /api path.
 * Request header required to have a matching
 * {@link org.zanata.mt.api.APIConstant.ID} and {@link org.zanata.mt.api.APIConstant.API_KEY} with request header
 * {@link org.zanata.mt.api.APIConstant.HEADER_USERNAME} and {@link org.zanata.mt.api.APIConstant.HEADER_API_KEY}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@WebFilter(filterName = "APISecurityFilter", value = { "/api/*" })
public class APISecurityFilter implements Filter {

    private static final Logger LOG =
        LoggerFactory.getLogger(APISecurityFilter.class);

    /**
     * Nonnull value. Verified during startup
     * {@link org.zanata.mt.service.Application#verifyCredentials
     */
    private static final RestCredentials REST_CREDENTIALS;

    static {
        REST_CREDENTIALS = new RestCredentials(System.getProperty(ID, ""),
            System.getProperty(API_KEY, ""));
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
            String error = "Cannot authenticate REST request: " + requestCredentials;
            LOG.info(error);
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, error);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private static class RestCredentials {
        private final Optional<String> username;
        private final Optional<String> apiKey;

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
        public String toString() {
            return "{" +
                "username=" + username +
                ", apiKey=" + apiKey +
                '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RestCredentials)) return false;

            RestCredentials that = (RestCredentials) o;

            if (username != null ? !username.equals(that.username) :
                that.username != null) return false;
            return apiKey != null ? apiKey.equals(that.apiKey) :
                that.apiKey == null;

        }

        @Override
        public int hashCode() {
            int result = username != null ? username.hashCode() : 0;
            result = 31 * result + (apiKey != null ? apiKey.hashCode() : 0);
            return result;
        }
    }
}
