package org.magpie.mt.api.service.impl;

import org.magpie.mt.api.service.LanguagesResource;
import org.magpie.mt.dao.LocaleDAO;
import org.magpie.mt.model.Locale;

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
        List<org.magpie.mt.api.dto.Locale> dtos = locales.stream()
                .map(locale -> new org.magpie.mt.api.dto.Locale(
                        locale.getLocaleCode().getId(), locale.getName()))
                .collect(Collectors.toList());
        return Response.ok().entity(dtos).build();
    }
}
