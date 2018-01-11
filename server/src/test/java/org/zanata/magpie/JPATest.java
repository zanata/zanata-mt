package org.zanata.magpie;

import org.jglue.cdiunit.ProducesAlternative;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class JPATest {

    private static EntityManagerFactory emf;

    protected static EntityManager em;

    @BeforeClass
    public static void initializeEMF() {
        emf = Persistence
                .createEntityManagerFactory("zanataMTDatasourcePUTest");
    }

    @Before
    public void setupEM() {
        emf.getCache().evictAll();
        em = emf.createEntityManager();
        em.getTransaction().begin();
        setupTestData();
    }

    @After
    public void shutdownEM() {
        em.getTransaction().rollback();
        if (em.isOpen()) {
            em.close();
        }
        em = null;
        emf.getCache().evictAll();
    }

    @Produces
    @ProducesAlternative
    @PersistenceContext
    public static EntityManager getEm() {
        return em;
    }

    protected abstract void setupTestData();
}
