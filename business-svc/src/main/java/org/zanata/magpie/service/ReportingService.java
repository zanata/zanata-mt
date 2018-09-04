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
package org.zanata.magpie.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.dto.MTRequestStatistics;
import org.zanata.magpie.dao.TextFlowMTRequestDAO;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.TextFlowMTRequest;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ApplicationScoped
public class ReportingService {
    private TextFlowMTRequestDAO textFlowMTRequestDAO;

    public ReportingService() {
    }

    @Inject
    public ReportingService(TextFlowMTRequestDAO textFlowMTRequestDAO) {
        this.textFlowMTRequestDAO = textFlowMTRequestDAO;
    }

    public List<MTRequestStatistics> getMTRequestStats(Account account, @Nonnull
            DateRange dateRange) {

        List<TextFlowMTRequest> requestsByDateRange =
                    textFlowMTRequestDAO.getRequestsByDateRange(dateRange, account);
        return requestsByDateRange.stream().map(request -> {
            Document document = request.getDocument();
            LocaleCode fromLocaleCode = document.getFromLocale().getLocaleCode();
            LocaleCode toLocaleCode = document.getToLocale().getLocaleCode();
            return new MTRequestStatistics(fromLocaleCode.getId(),
                    toLocaleCode.getId(), document.getUrl(),
                    request.getCharCount(), request.getWordCount(), request.getBackendID());
        }).collect(Collectors.toList());
    }
}
