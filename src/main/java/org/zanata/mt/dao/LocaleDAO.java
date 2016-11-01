package org.zanata.mt.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.Locale;

import com.ibm.icu.util.ULocale;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class LocaleDAO extends AbstractDAO<Locale> {
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public LocaleDAO() {
    }

    public Locale getByLocaleId(LocaleId localeId) {
        List<Locale> locales = entityManager
                .createQuery("from Locale where lower(localeId) = :localeId")
                .setParameter("localeId", localeId)
                .getResultList();
        return locales.isEmpty() ? null : locales.get(0);
    }

    @TransactionAttribute
    public Locale getOrCreateByLocaleId(LocaleId localeId) {
        Locale locale = getByLocaleId(localeId);

        if (locale == null) {
            ULocale uLocale = new ULocale(localeId.getId());
            locale = new Locale(localeId, uLocale.getDisplayName());
            locale = persist(locale);
            flush();
        }
        return locale;
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
