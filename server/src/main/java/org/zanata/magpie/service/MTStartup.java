package org.zanata.magpie.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.infinispan.Cache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.ClusteredCache;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.annotation.InitialPassword;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.event.AccountCreated;
import org.zanata.magpie.util.PasswordUtil;

import static org.zanata.magpie.producer.ResourceProducer.REPLICATE_CACHE;

/**
 * Startup monitor for MT.
 *
 * Insert any check needed when startup.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Singleton
@Startup
public class MTStartup {
    private static final Logger log =
        LoggerFactory.getLogger(MTStartup.class);

    public static final String APPLICATION_NAME = "Magpie service (Machine Translation)";

    protected static final String INITIAL_PASSWORD_CACHE = "initialPassword";

    private ConfigurationService configurationService;
    private AccountService accountService;

    private Cache<String, String> cache;

    public MTStartup() {
    }

    @Inject
    public MTStartup(ConfigurationService configurationService,
            AccountService accountService,
            @ClusteredCache(REPLICATE_CACHE)
            Cache<String, String> replCache) {
        this.configurationService = configurationService;
        this.accountService = accountService;
        cache = replCache;
    }

    @TransactionAttribute
    public void onStartUp(
        @Observes @Initialized ServletContext context,
            @DevMode boolean isDevMode, @BackEndProviders
            Set<BackendID> availableProviders)
        throws MTException {
        log.info("==========================================");
        log.info("==========================================");
        log.info("== " + APPLICATION_NAME + " ==");
        readManifestInfo(context);
        log.info("==========================================");
        log.info("==========================================");

        log.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (isDevMode) {
            log.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        log.info("Available backend providers: {}", availableProviders);

        showInitialAdminCredentialIfNoAccountExists();

        log.info("Default backend provider: {}",
                configurationService.getDefaultTranslationProvider(isDevMode));
    }

    private void readManifestInfo(ServletContext context) {
        String appServerHome = context.getRealPath("/");
        File manifestFile = new File(appServerHome, "META-INF/MANIFEST.MF");
        Attributes atts = null;
        if (manifestFile.canRead()) {
            Manifest mf = new Manifest();
            try (FileInputStream fis = new FileInputStream(manifestFile)) {
                mf.read(fis);
            } catch (IOException e) {
                log.warn("can not get manifest info: {}", e.getMessage());
            }
            atts = mf.getMainAttributes();
            String version = atts.getValue("Implementation-Version");
            String buildTimestamp = atts.getValue("Implementation-Build");
            String scmDescribe = atts.getValue("SCM-Describe");

            log.info("== version: {}", version);
            log.info("== build timestamp: {}", buildTimestamp);
            log.info("== scm describe: {}", scmDescribe);
        }
    }

    private void showInitialAdminCredentialIfNoAccountExists() {
        List<AccountDto> allAccounts = accountService.getAllAccounts(true);
        if (allAccounts.isEmpty()) {

            String initialPassword = getInitialPassword();
            log.info("=== no account exists in the system ===");
            log.info("=== to authenticate, use admin as username and ===");
            log.info("=== initial password (without leading spaces):  {}", initialPassword);

            cache.put(INITIAL_PASSWORD_CACHE, initialPassword);
        }
    }

    @NotNull
    private String getInitialPassword() {
        String valueInCache = cache.get(INITIAL_PASSWORD_CACHE);
        if (valueInCache == null) {
            log.info("no initial password yet. Generate one.");
            return new PasswordUtil().generateRandomPassword(32);
        }
        return valueInCache;
    }

    @Produces
    @InitialPassword
    public String getInitialPasswords() {
        return cache.get(INITIAL_PASSWORD_CACHE);
    }

    public void accountCreated(@Observes AccountCreated event) {
        if (event.getRoles().contains(Role.admin)
                && cache.get(INITIAL_PASSWORD_CACHE) != null) {
            log.info("admin account created. Removing intial password");
            cache.remove(INITIAL_PASSWORD_CACHE);
        }
        log.info("account created: {} {}", event.getEmail(), event.getRoles());
    }
}
