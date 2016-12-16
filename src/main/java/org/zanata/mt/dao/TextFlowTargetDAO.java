package org.zanata.mt.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.zanata.mt.model.TextFlowTarget;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class TextFlowTargetDAO extends AbstractDAO<TextFlowTarget> {
    private static final long serialVersionUID = -318395870569312481L;
    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public TextFlowTargetDAO() {
    }

    @VisibleForTesting
    public TextFlowTargetDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    EntityManager getEntityManager() {
        return entityManager;
    }
}
