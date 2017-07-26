package org.zanata.mt.dao;

import java.util.List;
import java.util.Optional;
import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.model.TextFlow;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class TextFlowDAO extends AbstractDAO<TextFlow> {
    private static final long serialVersionUID = -4593105065135284822L;

    @SuppressWarnings("unused")
    public TextFlowDAO() {
    }

    @VisibleForTesting
    public TextFlowDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }

    public Optional<TextFlow> getLatestByContentHash(LocaleCode localeCode,
            String contentHash) {
        String query =
                "FROM TextFlow WHERE contentHash =:contentHash AND locale.localeCode =:localeCode ORDER BY lastChanged DESC";
        List<TextFlow> tfs = getEntityManager()
                .createQuery(query)
                .setParameter("contentHash", contentHash)
                .setParameter("localeCode", localeCode)
                .getResultList();
        return (tfs == null || tfs.isEmpty()) ? Optional.empty() :
                Optional.of(tfs.get(0));
    }
}
