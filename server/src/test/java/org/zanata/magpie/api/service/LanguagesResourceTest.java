package org.zanata.magpie.api.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.magpie.api.dto.LocaleCode;
import org.zanata.magpie.api.service.impl.LanguagesResourceImpl;
import org.zanata.magpie.dao.LocaleDAO;
import org.zanata.magpie.model.Locale;

import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class LanguagesResourceTest {
    private LanguagesResource languagesResource;

    @Mock
    private LocaleDAO localeDAO;

    @Before
    public void setup() {
        languagesResource = new LanguagesResourceImpl(localeDAO);
    }

    @Test
    public void testConstructor() {
        LanguagesResource
                resource = new LanguagesResourceImpl();
    }

    @Test
    public void testGetSupportedLocales() {
        List<Locale> locales =
                ImmutableList.of(new Locale(LocaleCode.EN_US, "English"),
                        new Locale(LocaleCode.DE, "German"));
        List<org.zanata.magpie.api.dto.Locale> expectedLocales = ImmutableList.of(
                new org.zanata.magpie.api.dto.Locale("en-us", "English"),
                new org.zanata.magpie.api.dto.Locale("de", "German"));

        when(localeDAO.getSupportedLocales()).thenReturn(locales);

        Response response = languagesResource.getSupportedLanguages();
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        @SuppressWarnings("unchecked")
        List<org.zanata.magpie.api.dto.Locale> dtos = (List)response.getEntity();
        assertThat(dtos).isNotNull().hasSize(locales.size());
        assertThat(dtos.containsAll(expectedLocales)).isTrue();
    }

}
