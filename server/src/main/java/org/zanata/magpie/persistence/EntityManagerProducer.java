package org.zanata.magpie.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Producer in application - A wrapper over `@PersistenceContext`
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class EntityManagerProducer {

    @SuppressWarnings("unused")
    protected EntityManagerProducer() {
    }

    private EntityManager em;

    @PersistenceContext
    protected void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Produces
    protected EntityManager getEntityManager() {
        return em;
    }
}
