package org.zanata.mt.dao;

import org.zanata.mt.model.Document;
import org.zanata.mt.model.Locale;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class DocumentDAO extends AbstractDAO<Document> {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    EntityManager getEntityManager() {
        return entityManager;
    }

    public Document getByUrl(String url, Locale srcLocale, Locale targetLocale) {
        List<Document> documents = entityManager
            .createQuery("from Document where url = :url and srcLocale = :srcLocale and targetLocale = :targetLocale")
            .setParameter("url", url)
            .setParameter("srcLocale", srcLocale)
            .setParameter("targetLocale", targetLocale)
            .getResultList();
        return documents.isEmpty() ? null : documents.get(0);
    }

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
