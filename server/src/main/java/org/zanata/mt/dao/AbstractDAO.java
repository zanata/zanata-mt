package org.zanata.mt.dao;

import java.io.Serializable;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class AbstractDAO<T> implements Serializable {

    @Inject
    private EntityManager entityManager;

    void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    EntityManager getEntityManager() {
        return entityManager;
    }

    @TransactionAttribute
    public T persist(T entity) {
        return getEntityManager().merge(entity);
    }

    @TransactionAttribute
    public void flush() {
        getEntityManager().flush();
    }
}
