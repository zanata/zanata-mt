package org.zanata.magpie.api.service.impl;

import org.zanata.magpie.api.service.LanguagesResource;
import org.zanata.magpie.dao.LocaleDAO;
import org.zanata.magpie.model.Locale;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
public class LanguagesResourceImpl implements LanguagesResource {

    private LocaleDAO localeDAO;

    @SuppressWarnings("unused")
    public LanguagesResourceImpl() {
    }

    @Inject
    public LanguagesResourceImpl(LocaleDAO localeDAO) {
        this.localeDAO = localeDAO;
    }

    @Override
    public Response getSupportedLanguages() {
        List<Locale> locales = localeDAO.getSupportedLocales();
        List<org.zanata.magpie.api.dto.Locale> dtos = locales.stream()
                .map(locale -> new org.zanata.magpie.api.dto.Locale(
                        locale.getLocaleCode().getId(), locale.getName()))
                .collect(Collectors.toList());
        return Response.ok().entity(dtos).build();
    }
}
