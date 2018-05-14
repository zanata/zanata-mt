package org.zanata.magpie.dao;

import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.model.Document;
import org.zanata.magpie.model.Locale;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import org.zanata.magpie.dto.DateRange;
import org.zanata.magpie.util.HashUtil;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class DocumentDAO extends AbstractDAO<Document> {
    private static final long serialVersionUID = -2806219348294855687L;

    @SuppressWarnings("unused")
    public DocumentDAO() {
    }

    @VisibleForTesting
    DocumentDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public List<Document> getByUrl(@NotNull String url,
            Optional<LocaleCode> fromLocaleCode, Optional<LocaleCode> toLocaleCode,
            Optional<DateRange> dateParam) {
        String urlHash = HashUtil.generateHash(url);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from Document where urlHash =:urlHash");
        if (fromLocaleCode.isPresent()) {
            queryBuilder.append(" and fromLocale.localeCode =:fromLocaleCode");
        }
        if (toLocaleCode.isPresent()) {
            queryBuilder.append(" and toLocale.localeCode =:toLocaleCode");
        }
        if (dateParam.isPresent()) {
            queryBuilder.append(" and lastChanged between :fromDate and :toDate");
        }
        Query query = getEntityManager().createQuery(queryBuilder.toString())
                .setParameter("urlHash", urlHash);
        if (fromLocaleCode.isPresent()) {
            query.setParameter("fromLocaleCode", fromLocaleCode.get());
        }
        if (toLocaleCode.isPresent()) {
            query.setParameter("toLocaleCode", toLocaleCode.get());
        }
        if (dateParam.isPresent()) {
            query.setParameter("fromDate", dateParam.get().getFromDate());
            query.setParameter("toDate", dateParam.get().getToDate());
        }
        List<Document> documents = query.getResultList();
        return documents;
    }

    public Document getByUrl(@NotNull String url, @NotNull Locale fromLocale,
            @NotNull Locale toLocale) {
        List<Document> documents = getByUrl(url, Optional.of(fromLocale.getLocaleCode()),
                Optional.of(toLocale.getLocaleCode()), Optional.empty());
        return documents.isEmpty() ? null : documents.get(0);
    }

    public List<String> getUrlList(Optional<DateRange> dateParam) {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT DISTINCT url FROM Document");

        if (dateParam.isPresent()) {
            queryBuilder.append(" where lastChanged between :fromDate and :toDate");
        }

        Query query = getEntityManager().createQuery(queryBuilder.toString());
        if (dateParam.isPresent()) {
            query.setParameter("fromDate", dateParam.get().getFromDate());
            query.setParameter("toDate", dateParam.get().getToDate());
        }
        return query.getResultList();
    }
}
