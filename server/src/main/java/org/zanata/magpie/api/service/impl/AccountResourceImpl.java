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
package org.zanata.magpie.api.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.exception.ConstraintViolationException;
import org.zanata.magpie.annotation.CheckRole;
import org.zanata.magpie.api.APIConstant;
import org.zanata.magpie.api.AuthenticatedAccount;
import org.zanata.magpie.api.ValidatePayload;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.event.AccountCreated;
import org.zanata.magpie.exception.DataConstraintViolationException;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.service.AccountService;

import com.google.common.base.Throwables;
import com.webcohesion.enunciate.metadata.rs.RequestHeader;
import com.webcohesion.enunciate.metadata.rs.RequestHeaders;
import com.webcohesion.enunciate.metadata.rs.ResourceLabel;

/**
 * This is an internal API for managing users in the system.
 *
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Path("/account")
@RequestHeaders({
    @RequestHeader(name = APIConstant.HEADER_USERNAME,
        description = "The authentication user."),
    @RequestHeader(name = APIConstant.HEADER_API_KEY,
        description = "The authentication token.") })
@ResourceLabel("Account")
@Produces(MediaType.APPLICATION_JSON)
public class AccountResourceImpl {
    // expected format "Basic base64({username}:{password})"
    private static Pattern AUTH_HEADER_PATTERN =
        Pattern.compile("Basic\\s(.+)");

    // expected format "username:password"
    private static Pattern AUTH_DECODE_PATTERN = Pattern.compile("^(.+):(.+)");

    public final static String AUTH_HEADER = "Magpie-Auth";
    private final static String LOCALHOST = "localhost";

    private AccountService accountService;
    private Event<AccountCreated> accountCreatedEvent;
    private AuthenticatedAccount authenticatedAccount;

    // 1 hour
    private static final int COOKIE_MAX_AGE = 3600;


    // 1 min
    private static final int COOKIE_MIN_AGE = 60;

    @Context
    protected UriInfo uriInfo;

    @Inject
    public AccountResourceImpl(AccountService accountService,
        AuthenticatedAccount authenticatedAccount,
        Event<AccountCreated> accountCreatedEvent) {
        this.accountService = accountService;
        this.accountCreatedEvent = accountCreatedEvent;
        this.authenticatedAccount = authenticatedAccount;
    }

    @SuppressWarnings("unused")
    public AccountResourceImpl() {
    }

    @GET
    @CheckRole("admin")
    public List<AccountDto>
    getAllAccounts(@QueryParam("enabledOnly") boolean enabledOnly) {
        return accountService.getAllAccounts(!enabledOnly);
    }

    @ValidatePayload(AccountDto.class)
    @POST
    @CheckRole("admin")
    public Response registerNewAccount(AccountDto accountDto) {
        AccountDto dto =
            tryApplyChange(() -> accountService.registerNewAccount(
                accountDto, accountDto.getUsername(),
                accountDto.getPassword()));

        // as soon as we have an admin user, we should remove initial password
        accountCreatedEvent
            .fire(new AccountCreated(dto.getEmail(), dto.getRoles()));
        return Response.created(uriInfo.getRequestUriBuilder().path("id")
            .path(dto.getId().toString()).build()).build();
    }

    @ValidatePayload(AccountDto.class)
    @PUT
    @CheckRole("admin")
    public Response updateAccount(AccountDto accountDto) {
        Boolean updated =
            tryApplyChange(() -> accountService.updateAccount(accountDto));

        if (updated) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Login API.
     * Expecting: Authorization: Basic base64({username}:{password})
     *
     * @param authHeader
     * @return
     */
    @POST
    @Path("/login")
    public Response login(@HeaderParam("Authorization") String authHeader) {
        if (StringUtils.isBlank(authHeader)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Optional<Pair<String, String>> auth = processAuthHeader(authHeader);
        if (!auth.isPresent()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        String username = auth.get().getKey();
        String password = auth.get().getValue();
        return accountService.authenticate(username, password).map(account -> {
            authenticatedAccount.setAuthenticatedAccount(account);
            authenticatedAccount.setAuthenticatedUsername(username);
            NewCookie cookie = generateCookie(COOKIE_MAX_AGE, authHeader);

            return Response.ok().cookie(cookie).build();
        }).orElse(Response.status(Response.Status.UNAUTHORIZED).build());
    }

    /**
     * Logout API.
     * @return
     */
    @POST
    @Path("/logout")
    public Response logout() {
        authenticatedAccount.setAuthenticatedAccount(null);
        authenticatedAccount.setAuthenticatedUsername(null);
        NewCookie cookie = generateCookie(COOKIE_MIN_AGE, "");

        return Response.ok().cookie(cookie).build();
    }

    private NewCookie generateCookie(int maxAge, String header) {
        String domain = null;
        boolean secured = false;
        if (!LOCALHOST.equals(uriInfo.getBaseUri().getHost())) {
            domain = uriInfo.getBaseUri().getHost();
            secured = true;
        }
        return new NewCookie(AUTH_HEADER, header, "/", domain,
            NewCookie.DEFAULT_VERSION, "", maxAge, null, secured, true);
    }

    // TODO this is a temporary solution for having meaningful error message for constraint violation
    private static <T> T tryApplyChange(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            Optional<Throwable>
                anyConstraintViolation = Throwables.getCausalChain(e).stream()
                .filter(t -> t instanceof ConstraintViolationException)
                .findAny();
            if (anyConstraintViolation.isPresent()) {
                throw new DataConstraintViolationException(
                    Throwables.getRootCause(e));
            }
            throw new MTException("error registering new account", e);
        }
    }

    protected Optional<Pair<String, String>> processAuthHeader(
        @NotNull String authHeader) {
        Matcher headerMatcher = AUTH_HEADER_PATTERN.matcher(authHeader);
        if (headerMatcher.find() && headerMatcher.groupCount() == 1) {
            String base64Encoded = headerMatcher.group(1);
            byte[] decodedBytes = Base64.getDecoder().decode(base64Encoded);
            String decodedString =
                new String(decodedBytes, StandardCharsets.UTF_8);

            Matcher decodeMatcher = AUTH_DECODE_PATTERN.matcher(decodedString);
            if (decodeMatcher.find() && decodeMatcher.groupCount() == 2) {
                return Optional.of(Pair
                    .of(decodeMatcher.group(1), decodeMatcher.group(2)));
            }
        }
        return Optional.empty();
    }
}
