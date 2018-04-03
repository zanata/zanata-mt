package org.zanata.magpie.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.clustering.dispatcher.CommandResponse;
import org.wildfly.clustering.group.Node;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.security.AccountCreated;
import org.zanata.magpie.util.PasswordUtil;

/**
 * Startup monitor for MT.
 *
 * Insert any check needed when startup.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MTStartup {
    private static final Logger log =
        LoggerFactory.getLogger(MTStartup.class);

    public static final String APPLICATION_NAME = "Magpie service (Machine Translation)";

    private ConfigurationService configurationService;
    private AccountService accountService;

    @EJB
    private CommandDispatcherBean commandDispatcherBean;

    @Inject
    private InitialPasswordHolder initialPasswordHolder;



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
        log.info("==========================================");
        log.info("==========================================");
        log.info("== " + APPLICATION_NAME + " ==");
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

    private void showInitialAdminCredentialIfNoAccountExists() {
        List<AccountDto> allAccounts = accountService.getAllAccounts(true);
        if (allAccounts.isEmpty()) {
            String initialPassword = new PasswordUtil().generateRandomPassword(32);

            InitialPasswordCommand command =
                    new InitialPasswordCommand(initialPassword);
            try {
                Map<Node, CommandResponse<String>> responseMap =
                        commandDispatcherBean.executeOnCluster(command);

                responseMap.forEach((n, r) -> log.info("executed on node:{}", n));
                initialPasswordHolder.set(initialPassword);
            } catch (Exception e) {
                log.warn("failed to execute on nodes", e);
            }
        }
    }

    protected void accountCreated(@Observes AccountCreated event) {
        if (event.getRoles().contains(Role.admin)
                && initialPasswordHolder.hasInitialPassword()) {

            try {
                InvalidateInitialPasswordCommand
                        command =
                        new InvalidateInitialPasswordCommand();
                Map<Node, CommandResponse<String>> responseMap =
                        commandDispatcherBean.executeOnCluster(command);

                responseMap.forEach((n, r) -> log.info("executed on node:{}", n));
            } catch (Exception e) {
                log.warn("failed to execute on nodes", e);
            } finally {
                initialPasswordHolder.unset();
            }
        }
        log.info("account created: {} {}", event.getEmail(), event.getRoles());
    }
}
