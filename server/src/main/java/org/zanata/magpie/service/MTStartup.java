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
import org.zanata.magpie.model.Role;
import org.zanata.magpie.security.AccountCreated;
import org.zanata.magpie.util.PasswordUtil;
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

    public static final String APPLICATION_NAME = "Magpie service (Machine Translation)";

    private ConfigurationService configurationService;
    private AccountService accountService;
    private String initialPassword = null;
    private static final Path INITIAL_PASSWORD_FILE = Paths.get(System.getProperty("user.home"),
            "magpie_initial_password");

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
        LOG.info("==========================================");
        LOG.info("==========================================");
        LOG.info("== " + APPLICATION_NAME + " ==");
        LOG.info("==========================================");
        LOG.info("==========================================");
        LOG.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (isDevMode) {
            LOG.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        LOG.info("Available backend providers: {}", availableProviders);

        showInitialAdminCredentialIfNoAccountExists();

        LOG.info("Default backend provider: {}",
                configurationService.getDefaultTranslationProvider(isDevMode));
    }

    private void showInitialAdminCredentialIfNoAccountExists() {
        List<AccountDto> allAccounts = accountService.getAllAccounts(true);
        if (allAccounts.isEmpty()) {
            initialPassword = new PasswordUtil().generateRandomPassword(32);
            LOG.info("=== no account exists in the system ===");
            LOG.info("=== to authenticate, use admin as username and ===");
            LOG.info("=== initial password (without leading spaces):  {}", initialPassword);
            LOG.info("=== initial password is also written to:  {}",
                    INITIAL_PASSWORD_FILE);
            LOG.info("=======================================");

            try {
                Files.write(INITIAL_PASSWORD_FILE,
                        Lists.newArrayList(this.initialPassword));
            } catch (IOException e) {
                LOG.warn("failed writing initial password to disk", e);
            }
            try {
                Runtime.getRuntime()
                        .exec(new String[] {"chmod", "400", INITIAL_PASSWORD_FILE
                                .toAbsolutePath().toString()});
            } catch (IOException e) {
                LOG.info("unable to change permission on {}",
                        INITIAL_PASSWORD_FILE);
            }
        }
    }

    @Produces
    @InitialPassword
    protected String initialPassword() {
        return initialPassword;
    }

    protected void accountCreated(@Observes AccountCreated event) {
        if (event.getRoles().contains(Role.admin) && initialPassword != null) {
            try {
                Files.delete(INITIAL_PASSWORD_FILE);
            } catch (IOException e) {
                LOG.warn("unable to delete {}. {}", INITIAL_PASSWORD_FILE, e.getMessage());
            }
            initialPassword = null;
        }
        LOG.info("account created: {} {}", event.getEmail(), event.getRoles());
    }
}
