package org.zanata.magpie.dao;

import java.util.List;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.model.Locale;
import org.zanata.magpie.model.LocaleAlias;
import com.google.common.annotations.VisibleForTesting;
import com.ibm.icu.util.ULocale;

@RequestScoped
public class LocaleAliasDAO extends AbstractDAO<LocaleAlias> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    public LocaleAliasDAO() {
    }

    @VisibleForTesting
    LocaleAliasDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public @Nullable LocaleAlias getByLocaleCode(LocaleCode localeCode) {
        @SuppressWarnings("unchecked")
        List<LocaleAlias> locales = getEntityManager()
                .createQuery("from LocaleAlias where lower(localeCode) = :localeCode")
                .setParameter("localeCode", localeCode)
                .getResultList();
        return locales.isEmpty() ? null : locales.get(0);
    }

}
