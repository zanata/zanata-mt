package org.zanata.magpie.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.infinispan.Cache;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.magpie.annotation.BackEndProviders;
import org.zanata.magpie.annotation.ClusteredCache;
import org.zanata.magpie.annotation.DevMode;
import org.zanata.magpie.annotation.InitialPassword;
import org.zanata.magpie.api.dto.AccountDto;
import org.zanata.magpie.event.AccountCreated;
import org.zanata.magpie.model.BackendID;
import org.zanata.magpie.model.Role;
import org.zanata.magpie.util.PasswordUtil;
import com.google.common.collect.ImmutableList;

import static org.zanata.magpie.producer.ResourceProducer.REPLICATE_CACHE;

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

    protected static final String INITIAL_PASSWORD_CACHE = "initialPassword";
    private static final Path INITIAL_PASSWORD_FILE = Paths.get(System.getProperty("user.home"),
            "magpie_initial_password");

    private Cache<String, String> cache;

    public MTStartup() {
    }

    @Inject
    public MTStartup(ConfigurationService configurationService,
            AccountService accountService,
            @ClusteredCache(REPLICATE_CACHE)
            Cache<String, String> replCache,
            @DeltaSpike ServletContext servletContext,
            @DevMode boolean isDevMode,
            @BackEndProviders
            Set<BackendID> availableProviders) {
        this.cache = replCache;

        log.info("==========================================");
        log.info("==========================================");
        log.info("== " + APPLICATION_NAME + " ==");
        readManifestInfo(servletContext);
        log.info("==========================================");
        log.info("==========================================");

        log.info("Build info: version-" + configurationService.getVersion() +
                " date-" + configurationService.getBuildDate());
        if (isDevMode) {
            log.warn("THIS IS A DEV MODE BUILD. DO NOT USE IT FOR PRODUCTION");
        }
        log.info("Available backend providers: {}", availableProviders);

        showInitialAdminCredentialIfNoAccountExists(accountService);

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

    private void showInitialAdminCredentialIfNoAccountExists(
            AccountService accountService) {
        List<AccountDto> allAccounts = accountService.getAllAccounts(true);
        if (allAccounts.isEmpty()) {

            String initialPassword = getInitialPassword();
            log.info("There are no accounts in the system.");
            log.info("To create an account via REST, use admin as username and");
            log.info("initial password (without leading space): {}", initialPassword);

            cache.put(INITIAL_PASSWORD_CACHE, initialPassword);
            writeInitialPasswordToFile(initialPassword);
        }
    }

    private static void writeInitialPasswordToFile(String initialPassword) {
        try {
            log.info("Writing initial password to file: {}", INITIAL_PASSWORD_FILE);
            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
            FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);
            Files.createFile(INITIAL_PASSWORD_FILE, attr);
            Files.write(INITIAL_PASSWORD_FILE, ImmutableList.of(initialPassword));
        } catch (IOException e) {
            log.warn("failed writing initial password to disk", e);
        }
    }

    @NotNull
    private String getInitialPassword() {
        String valueInCache = cache.get(INITIAL_PASSWORD_CACHE);
        if (valueInCache == null) {
            log.info("No initial password yet. Generating one.");
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
            try {
                Files.delete(INITIAL_PASSWORD_FILE);
            } catch (IOException e) {
                log.warn("unable to delete {}. {}", INITIAL_PASSWORD_FILE, e.getMessage());
            }
        }
        log.info("account created: {} {}", event.getEmail(), event.getRoles());
    }
}
