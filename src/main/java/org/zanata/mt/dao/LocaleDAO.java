package org.zanata.mt.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;

import com.ibm.icu.util.ULocale;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.Locale;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class LocaleDAO extends AbstractDAO<Locale> {
    private static final long serialVersionUID = -1640472923498327999L;

    @SuppressWarnings("unused")
    public LocaleDAO() {
    }

    @VisibleForTesting
    LocaleDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public Locale getByLocaleId(LocaleId localeId) {
        List<Locale> locales = getEntityManager()
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

    public List<Locale> getSupportedLocales() {
        return getEntityManager()
                .createQuery("from Locale").getResultList();
    }
}
