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
package org.zanata.magpie.security;

import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.AbstractAccessDecisionVoter;
import org.apache.deltaspike.security.api.authorization.AccessDecisionVoterContext;
import org.apache.deltaspike.security.api.authorization.SecurityViolation;
import org.zanata.magpie.annotation.Authenticated;
import org.zanata.magpie.annotation.CheckRole;
import org.zanata.magpie.model.Account;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
public class CheckRoleDecisionVoter extends AbstractAccessDecisionVoter {
    private static final long serialVersionUID = -2225527674677560626L;

    @Inject
    @Authenticated
    private Account account;

    @Override
    protected void checkPermission(
            AccessDecisionVoterContext accessDecisionVoterContext,
            Set<SecurityViolation> violations) {

        CheckRole hasRole =
                accessDecisionVoterContext
                        .getMetaDataFor(CheckRole.class.getName(),
                                CheckRole.class);
        if (hasRole == null) {
            return;
        }
        if (account == null) {
            violations.add(newSecurityViolation("Not authenticated"));
        }
        if (!account.getRoles().contains(hasRole.value())) {
            violations.add(newSecurityViolation("You don't have the necessary access"));
        }

    }
}
