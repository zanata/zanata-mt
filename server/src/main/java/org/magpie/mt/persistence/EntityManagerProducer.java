package org.magpie.mt.persistence;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Producer in application - A wrapper over `@PersistenceContext`
 *
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class EntityManagerProducer {

    @SuppressWarnings("unused")
    protected EntityManagerProducer() {
    }

    @Produces
    @PersistenceContext
    private EntityManager em;
}
