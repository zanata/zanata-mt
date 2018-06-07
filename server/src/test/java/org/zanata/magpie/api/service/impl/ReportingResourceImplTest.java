package org.zanata.magpie.api.service.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.AuthenticatedAccount;
import org.zanata.magpie.api.dto.MTRequestStatistics;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.service.ReportingService;

import com.google.common.collect.Lists;

public class ReportingResourceImplTest {

    private ReportingResourceImpl resource;
    @Mock private ReportingService reportingService;
    private AuthenticatedAccount authenticatedAccount;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authenticatedAccount = new AuthenticatedAccount();
        resource = new ReportingResourceImpl(reportingService, authenticatedAccount);
    }

    @Test
    public void returnUnauthorizedIfNoAuthenticatedAccount() {
        authenticatedAccount.setAuthenticatedAccount(null);
        DateRange dateRange = DateRange.fromString("2018-05-01..2018-05-30");

        Response response = resource.getMachineTranslationUsage(
                dateRange);
        assertThat(response.getStatus()).isEqualTo(401);
    }

    @Test
    public void canGetMTUsageReportForCurrentUser() {
        Account account = new Account();
        authenticatedAccount.setAuthenticatedAccount(account);
        DateRange dateRange = DateRange.fromString("2018-05-01..2018-05-30");
        List<MTRequestStatistics> stats =
                Lists.newArrayList(new MTRequestStatistics("en", "ja",
                        "https://example.com", 10, 4, BackendID.DEV));
        when(reportingService.getMTRequestStats(account, dateRange))
                .thenReturn(stats);

        Response response = resource.getMachineTranslationUsage(
                dateRange);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(stats);
    }

}
