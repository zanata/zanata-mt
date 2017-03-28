package org.zanata.mt.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.zanata.mt.api.dto.LocaleId;
import org.zanata.mt.model.Locale;

import com.google.common.annotations.VisibleForTesting;
import com.ibm.icu.util.ULocale;

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

    public Locale generateLocale(LocaleId localeId) {
        ULocale uLocale = new ULocale(localeId.getId());
        return new Locale(localeId, uLocale.getDisplayName());
    }
}
