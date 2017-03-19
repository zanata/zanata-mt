package org.zanata.mt.dao;

import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import java.util.List;

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

    public Document getByUrl(String url, Locale srcLocale, Locale targetLocale) {
        String urlHash = HashUtil.generateHash(url);
        List<Document> documents = getEntityManager()
            .createQuery("from Document where urlHash = :urlHash and srcLocale = :srcLocale and targetLocale = :targetLocale")
            .setParameter("urlHash", urlHash)
            .setParameter("srcLocale", srcLocale)
            .setParameter("targetLocale", targetLocale)
            .getResultList();
        return documents.isEmpty() ? null : documents.get(0);
    }

    @TransactionAttribute
    public Document getOrCreateByUrl(String url, Locale srcLocale, Locale targetLocale) {
        Document doc = getByUrl(url, srcLocale, targetLocale);

        if (doc == null) {
            doc = new Document(url, srcLocale, targetLocale);
            doc = persist(doc);
            flush();
        }
        return doc;
    }
}
