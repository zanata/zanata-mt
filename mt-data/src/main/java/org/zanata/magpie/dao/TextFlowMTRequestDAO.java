package org.zanata.magpie.dao;

import java.util.List;

import javax.annotation.Nonnull;

import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.model.Account;
import org.zanata.magpie.model.TextFlowMTRequest;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TextFlowMTRequestDAO extends AbstractDAO<TextFlowMTRequest> {
    private static final long serialVersionUID = 6450913732912755129L;

    public TextFlowMTRequestDAO() {
    }

    public List<TextFlowMTRequest> getRequestsByDateRange(
            @Nonnull DateRange dateRange, @Nonnull Account triggeredBy) {
        return getEntityManager().createQuery(
                "from TextFlowMTRequest r where r.triggeredBy = :account and invokeDate between :fromDate and :toDate",
                TextFlowMTRequest.class)
                .setParameter("account", triggeredBy)
                .setParameter("fromDate", dateRange.getFromDate())
                .setParameter("toDate", dateRange.getToDate())
                .getResultList();

    }
}
