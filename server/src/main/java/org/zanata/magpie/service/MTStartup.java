package org.zanata.magpie.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.deltaspike.core.api.lifecycle.Initialized;
import org.infinispan.Cache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.clustering.dispatcher.CommandResponse;
import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.ClusteredCache;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.annotation.InitialPassword;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.exception.MTException;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.security.AccountCreated;
import org.zanata.magpie.util.PasswordUtil;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

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

    public static final String INITIAL_PASSWORD_CACHE = "initialPassword";

    private ConfigurationService configurationService;
    private AccountService accountService;

    @EJB
    private CommandDispatcherBean commandDispatcherBean;

    @Resource(lookup = "java:jboss/clustering/group/web")
    private Group channelGroup;

    @Inject @ClusteredCache(INITIAL_PASSWORD_CACHE)
    private Cache<String, String> cache;

    @Inject @ClusteredCache(INITIAL_PASSWORD_CACHE)
    private TransactionManager transactionManager;

    private Set<String> initialPasswords = Sets.newHashSet();

    public MTStartup() {
    }

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

            String initialPassword = getInitialPassword();
            log.info("=== no account exists in the system ===");
            log.info("=== to authenticate, use admin as username and ===");
            log.info("=== initial password (without leading spaces):  {}", initialPassword);

            InitialPasswordCommand command =
                    new InitialPasswordCommand(initialPassword);
//            try {

                channelGroup.addListener((prevNodes, currentNodes, isMerged) -> {
                    log.info("--- previous nodes:{}", prevNodes.stream().map(Node::getName).collect(
                            Collectors.toList()));
                    log.info("--- current nodes:{}", currentNodes.stream().map(Node::getName).collect(
                            Collectors.toList()));
                    log.info("--- is Merged:{}", isMerged);

                    try {
                        Map<Node, CommandResponse<String>> responseMap =
                                commandDispatcherBean.executeOnCluster(command);
                        responseMap.forEach((n, r) -> {
                            log.info("executed on node:{}", n);
                            try {
                                log.info("--- adding initialPassword to set: {}", initialPasswords);
                                initialPasswords.add(r.get());
                            } catch (ExecutionException e) {
                                log.error("fail getting response from cluster execution", e);
                            }
                        });


                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

//            try {
//                transactionManager.begin();
//                cache.put(INITIAL_PASSWORD_CACHE, initialPassword);
//                transactionManager.commit();
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            } catch (Exception e) {
//                log.warn("failed to execute on nodes", e);
//            }

//            ImmutableList<String> keys =
//                    ImmutableList.copyOf(cache.keySet());
//            log.info("---- current keys: {}", keys);
        }
    }

//    @Produces
//    @InitialPassword
    @NotNull
    private String getInitialPassword() {
//        String valueInCache = cache.get(INITIAL_PASSWORD_CACHE);
//        if (valueInCache == null) {
//            log.info("no initial password yet. Generate one.");
            return new PasswordUtil().generateRandomPassword(32);
//        }
//        return valueInCache;
    }

    @Produces
    @InitialPassword
    public Set<String> getInitialPasswords() {
        return initialPasswords;
    }

    public void accountCreated(@Observes AccountCreated event) {
        if (event.getRoles().contains(Role.admin)
                && cache.get(INITIAL_PASSWORD_CACHE) != null) {

//            try {
//                InvalidateInitialPasswordCommand
//                        command =
//                        new InvalidateInitialPasswordCommand();
//                Map<Node, CommandResponse<String>> responseMap =
//                        commandDispatcherBean.executeOnCluster(command);
//
//                responseMap.forEach((n, r) -> log.info("executed on node:{}", n));
//            } catch (Exception e) {
//                log.warn("failed to execute on nodes", e);
//            } finally {
//            }
            cache.remove(INITIAL_PASSWORD_CACHE);
        }
        log.info("account created: {} {}", event.getEmail(), event.getRoles());
    }
}
