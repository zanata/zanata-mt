package org.zanata.mt.backend.mock;

import org.zanata.mt.api.dto.LocaleCode;
import org.zanata.mt.backend.BackendLocaleCode;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.AugmentedTranslation;
import org.zanata.mt.service.TranslatorBackend;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock backend service for when DEV mode is enabled.
 * See {@link org.zanata.mt.model.BackendID#DEV}
 *
 * This service will return translations of original string with prefix of
 * {@link #PREFIX_MOCK_STRING}.
 *
 * See {@link org.zanata.mt.service.ZanataMTStartup#isDevMode}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@ApplicationScoped
public class MockTranslatorBackend implements TranslatorBackend {
    public final static String PREFIX_MOCK_STRING = "translated:";

    @SuppressWarnings("unused")
    public MockTranslatorBackend() {
    }

    @Override
    public AugmentedTranslation translate(String content,
            BackendLocaleCode srcLocale, BackendLocaleCode targetLocale,
            MediaType mediaType) throws ZanataMTException {
        return new AugmentedTranslation(PREFIX_MOCK_STRING + content,
                PREFIX_MOCK_STRING + content);
    }

    @Override
    public List<AugmentedTranslation> translate(List<String> contents,
            BackendLocaleCode srcLocale, BackendLocaleCode targetLocale,
            MediaType mediaType) throws ZanataMTException {
        List<AugmentedTranslation> translations = contents.stream()
                .map(source -> new AugmentedTranslation(
                        PREFIX_MOCK_STRING + source,
                        PREFIX_MOCK_STRING + source))
                .collect(Collectors.toList());
        return translations;
    }

    /**
     * Return same localeCode as parsed in
     */
    @Override
    public BackendLocaleCode getMappedLocale(LocaleCode localeCode) {
        return new MockLocaleCode(localeCode);
    }

    public static class MockLocaleCode implements BackendLocaleCode {
        private String localeCode;

        public MockLocaleCode(@NotNull LocaleCode localeCode) {
            this.localeCode = localeCode.getId();
        }

        @Override
        public String getLocaleCode() {
            return localeCode;
        }
    }
}
