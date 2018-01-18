package org.zanata.magpie.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.annotation.InitialPassword;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.security.UnsetInitialPassword;
import org.zanata.magpie.util.PasswordUtil;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

/**
 * Startup monitor for MT.
 *
 * Insert any check needed when startup.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MTStartup {
    private static final Logger LOG =
        LoggerFactory.getLogger(MTStartup.class);

    private ConfigurationService configurationService;
    private AccountService accountService;
    private String initialPassword = null;

    @Inject
    public MTStartup(ConfigurationService configurationService, AccountService accountService) {
        this.configurationService = configurationService;
        this.accountService = accountService;
    }

    public void onStartUp(
        @Observes @Initialized ServletContext context,
            @DevMode boolean isDevMode, @BackEndProviders
            Set<BackendID> availableProviders)
        throws MTException {
        LOG.info("===================================");
        LOG.info("===================================");
        LOG.info("=== Machine Translation Service ===");
        LOG.info("===================================");
        LOG.info("===================================");
        LOG.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (isDevMode) {
            LOG.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        LOG.info("Available backend providers: {}", availableProviders);

        showInitialAdminCredentialIfNoAccountExists();

    }

    private void showInitialAdminCredentialIfNoAccountExists() {
        List<AccountDto> allAccounts = accountService.getAllAccounts(true);
        if (allAccounts.isEmpty()) {
            initialPassword = new PasswordUtil().generateRandomPassword(32);
            Path initialPasswordFile =
                    Paths.get(System.getProperty("user.home"),
                            "initialPassword");
            LOG.info("=== no account exists in the system ===");
            LOG.info("=== to authenticate, use admin as username and ===");
            LOG.info("=== initial password (without leading spaces):  {}", initialPassword);
            LOG.info("=== initial password is also written to:  {}", initialPasswordFile);
            LOG.info("=======================================");
            try {
                Files.write(initialPasswordFile,
                        Lists.newArrayList(this.initialPassword));
            } catch (IOException e) {
                LOG.warn("failed writing initial password to disk", e);
            }
        }
    }

    @Produces
    @InitialPassword
    protected String initialPassword() {
        return initialPassword;
    }

    protected void unsetInitialPassword(@Observes UnsetInitialPassword event) {
        initialPassword = null;
    }
}
