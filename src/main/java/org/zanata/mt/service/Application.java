package org.zanata.mt.service;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.exception.ZanataMTException;

/**
 * Startup monitor for Zanata MT.
 *
 * Insert any check needed when startup.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class Application {
    private static final Logger LOG =
        LoggerFactory.getLogger(Application.class);

    public void onStartUp(
        @Observes @Initialized(ApplicationScoped.class) Object init)
        throws ZanataMTException {
        LOG.info("============================================");
        LOG.info("============================================");
        LOG.info("=====Zanata Machine Translation Service=====");
        LOG.info("============================================");
        LOG.info("============================================");
    }
}
