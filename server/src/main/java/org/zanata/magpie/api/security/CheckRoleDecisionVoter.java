/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.magpie.api.security;

import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.zanata.magpie.annotation.CheckRole;
import org.zanata.magpie.api.AuthenticatedAccount;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class CheckRoleDecisionVoter extends AbstractAccessDecisionVoter {
    private static final long serialVersionUID = -2225527674677560626L;

    private AuthenticatedAccount authenticatedAccount;

    @SuppressWarnings("unused")
    public CheckRoleDecisionVoter() {
    }

    @Inject
    public CheckRoleDecisionVoter(AuthenticatedAccount authenticatedAccount) {
        this.authenticatedAccount = authenticatedAccount;
    }

    @Override
    protected void checkPermission(
            AccessDecisionVoterContext accessDecisionVoterContext,
            Set<SecurityViolation> violations) {

        CheckRole hasRole =
                accessDecisionVoterContext
                        .getMetaDataFor(CheckRole.class.getName(),
                                CheckRole.class);

        if (!authenticatedAccount.hasAuthenticatedAccount()) {
            violations.add(newSecurityViolation("Not authenticated"));
        } else if (hasRole != null && !authenticatedAccountHasRole(hasRole.value())) {
            violations.add(newSecurityViolation("You don't have the necessary access"));
        }

    }

    private boolean authenticatedAccountHasRole(String roleName) {
        return authenticatedAccount.getAuthenticatedAccount().flatMap(a ->
                a.hasRole(roleName) ? Optional.of(a) : Optional.empty()).isPresent();
    }
}
