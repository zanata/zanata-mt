package org.zanata.mt.dao;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;

import org.zanata.mt.model.TextFlowTarget;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Stateless
public class TextFlowTargetDAO extends AbstractDAO<TextFlowTarget> {
    private static final long serialVersionUID = -318395870569312481L;

    @SuppressWarnings("unused")
    public TextFlowTargetDAO() {
    }

    @VisibleForTesting
    TextFlowTargetDAO(EntityManager entityManager) {
        setEntityManager(entityManager);
    }
}
