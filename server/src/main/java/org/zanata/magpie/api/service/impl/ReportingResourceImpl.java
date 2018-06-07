/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.zanata.magpie.api.AuthenticatedAccount;
import org.zanata.magpie.api.dto.MTRequestStatistics;
import org.zanata.magpie.api.service.ReportingResource;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.service.ReportingService;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/report")
public class ReportingResourceImpl implements ReportingResource {
    private ReportingService reportingService;
    private AuthenticatedAccount authenticatedAccount;

    public ReportingResourceImpl() {
    }

    @Inject
    public ReportingResourceImpl(
            ReportingService reportingService,
            AuthenticatedAccount authenticatedAccount) {
        this.reportingService = reportingService;
        this.authenticatedAccount = authenticatedAccount;
    }

    @Override
    public Response getMachineTranslationUsage(DateRange dateRange) {

        Optional<Account> account =
                this.authenticatedAccount.getAuthenticatedAccount();
        if (!account.isPresent()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<MTRequestStatistics> stats = reportingService
                .getMTRequestStats(account.get(), dateRange);
        GenericEntity<List<MTRequestStatistics>> entity =
                new GenericEntity<List<MTRequestStatistics>>(stats) {
                };
        return Response.ok(entity).build();
    }
}
