package org.zanata.magpie.dao;

import java.util.List;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import com.ibm.icu.util.ULocale;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.model.Locale;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class LocaleDAO extends AbstractDAO<Locale> {
    private static final long serialVersionUID = -1640472923498327999L;

    @SuppressWarnings("unused")
    public LocaleDAO() {
    }

    @VisibleForTesting
    LocaleDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public @Nullable Locale getByLocaleCode(LocaleCode localeCode) {
        @SuppressWarnings("unchecked")
        List<Locale> locales = getEntityManager()
                .createQuery("from Locale where lower(localeCode) = :localeCode")
                .setParameter("localeCode", localeCode)
                .getResultList();
        return locales.isEmpty() ? null : locales.get(0);
    }

    public Locale getOrCreateByLocaleCode(LocaleCode localeCode) {
        Locale locale = getByLocaleCode(localeCode);

        if (locale == null) {
            ULocale uLocale = new ULocale(localeCode.getId());
            locale = new Locale(localeCode, uLocale.getDisplayName());
            locale = persist(locale);
            flush();
        }
        return locale;
    }

    public List<Locale> getSupportedLocales() {
        //noinspection unchecked
        return getEntityManager()
                .createQuery("from Locale").getResultList();
    }
}
