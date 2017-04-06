package org.zanata.mt.dao;

import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import org.zanata.mt.util.HashUtil;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
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
            Optional<LocaleId> fromLocaleCode,
            Optional<LocaleId> toLocaleCode) {
        String urlHash = HashUtil.generateHash(url);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("from Document where urlHash =:urlHash");
        if (fromLocaleCode.isPresent()) {
            queryBuilder.append(" and srcLocale.localeId =:fromLocaleCode");
        }
        if (toLocaleCode.isPresent()) {
            queryBuilder.append(" and targetLocale.localeId =:toLocaleCode");
        }
        Query query = getEntityManager().createQuery(queryBuilder.toString())
                .setParameter("urlHash", urlHash);
        if (fromLocaleCode.isPresent()) {
            query.setParameter("fromLocaleCode", fromLocaleCode.get());
        }
        if (toLocaleCode.isPresent()) {
            query.setParameter("toLocaleCode", toLocaleCode.get());
        }
        List<Document> documents = query.getResultList();
        return documents;
    }

    public Document getByUrl(@NotNull String url, @NotNull Locale fromLocale,
            @NotNull Locale toLocale) {
        List<Document> documents = getByUrl(url, Optional.of(fromLocale.getLocaleId()),
                Optional.of(toLocale.getLocaleId()));
        return documents.isEmpty() ? null : documents.get(0);
    }

    @TransactionAttribute
    public Document getOrCreateByUrl(String url, Locale fromLocale, Locale toLocale) {
        Document doc = getByUrl(url, fromLocale, toLocale);

        if (doc == null) {
            doc = new Document(url, fromLocale, toLocale);
            doc = persist(doc);
            flush();
        }
        return doc;
    }

    public List<String> getUrlList() {
        return getEntityManager()
                .createQuery("SELECT DISTINCT url FROM Document")
                .getResultList();
    }
}
