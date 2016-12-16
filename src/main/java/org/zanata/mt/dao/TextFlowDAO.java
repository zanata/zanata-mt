package org.zanata.mt.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.zanata.mt.model.TextFlow;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class TextFlowDAO extends AbstractDAO<TextFlow> {
    private static final long serialVersionUID = -4593105065135284822L;
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public TextFlowDAO() {
    }

    @VisibleForTesting
    public TextFlowDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public TextFlow getByHash(String hash) {
        List<TextFlow> tfs = entityManager
                .createQuery(
                        "from TextFlow where hash =:hash")
                .setParameter("hash", hash)
                .getResultList();
        return (tfs == null || tfs.isEmpty()) ? null : tfs.get(0);
    }

    @Override
    EntityManager getEntityManager() {
        return entityManager;
    }
}
