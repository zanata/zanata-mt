package org.zanata.magpie.service;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.MTRequestStatistics;
import org.zanata.magpie.dao.AccountDAO;
import org.zanata.magpie.dao.TextFlowMTRequestDAO;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.TextFlowMTRequest;
import com.google.common.collect.ImmutableList;

public class ReportingServiceTest {
    @Mock
    private AccountDAO accountDAO;
    @Mock
    private TextFlowMTRequestDAO requestDAO;
    private ReportingService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        service = new ReportingService(accountDAO, requestDAO);
    }

    @Test
    public void canQueryMTRequests() {
        DateRange dateRange = DateRange.fromString("2018-05-01..2018-05-31");
        Account account = new Account();
        when(accountDAO.findAccountByUsername("admin")).thenReturn(of(account));
        Locale toLocale = new Locale(LocaleCode.ZH_HANS, "Simplified Chinese");
        Locale fromLocale = new Locale(LocaleCode.EN_US, "English");
        int charCount = 10;
        int wordCount = 4;
        when(requestDAO.getRequestsByDateRange(dateRange, account))
                .thenReturn(ImmutableList.of(
                        new TextFlowMTRequest(BackendID.DEV, new Date(),
                                new Document("https://example.com", fromLocale,
                                        toLocale),
                                account, ImmutableList.of("abc"), wordCount,
                                charCount)));
        List<MTRequestStatistics> requestStats =
                service.getMTRequestStats(account, dateRange);

        assertThat(requestStats).hasSize(1);
        MTRequestStatistics stats = requestStats.get(0);
        assertThat(stats.getCharCount()).isEqualTo(charCount);
        assertThat(stats.getWordCount()).isEqualTo(wordCount);
        assertThat(stats.getDocUrl()).isEqualTo("https://example.com");
    }

}
