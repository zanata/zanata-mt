package org.zanata.mt.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.zanata.mt.model.TextFlowTarget;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class TextFlowTargetDAO extends AbstractDAO<TextFlowTarget> {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public TextFlowTargetDAO() {
    }

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
