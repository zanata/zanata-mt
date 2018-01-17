/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.magpie.api;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.InitialPassword;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.AccountType;
import org.zanata.magpie.service.AccountService;
import com.google.common.collect.Sets;

@javax.ws.rs.ext.Provider
@RequestScoped
public class SecurityInterceptor implements ContainerRequestFilter {
    private static final Logger log =
            LoggerFactory.getLogger(SecurityInterceptor.class);
    private Provider<String> initialPassword;
    private AccountService accountService;
    private AuthenticatedAccount authenticatedAccount;

    @Inject
    public SecurityInterceptor(
            @InitialPassword Provider<String> initialPassword,
            AccountService accountService,
            AuthenticatedAccount authenticatedAccount) {
        this.initialPassword = initialPassword;
        this.accountService = accountService;
        this.authenticatedAccount = authenticatedAccount;
    }

    @Override
    public void filter(ContainerRequestContext requestContext)
            throws IOException {
        String username = requestContext.getHeaderString("X-Auth-User");
        String token = requestContext.getHeaderString("X-Auth-Token");
        if (username != null && token != null) {
            Optional<Account> account = tryAuthenticate(username, token);
            if (account.isPresent()) {
                log.debug("authenticated {}", username);
                authenticatedAccount.setAuthenticatedAccount(account.get());
            }
        } else {
            requestContext.abortWith(Response.status(Status.UNAUTHORIZED).build());
        }
    }

    private Optional<Account> tryAuthenticate(String username, String token) {
        String initialPass = initialPassword.get();
        if (initialPass == null) {
            return accountService.authenticate(username, token);
        } else if (Objects.equals("admin", username) && Objects.equals(token, initialPass)) {
            log.info("authenticating using initial password");
            Account initialAdmin = new Account("initial", "magpie@zanata.org",
                    AccountType.Normal,
                    Sets.newHashSet("admin"));
            return Optional.of(initialAdmin);
        }
        return Optional.empty();
    }


    @SuppressWarnings("unused")
    public SecurityInterceptor() {
    }
}
