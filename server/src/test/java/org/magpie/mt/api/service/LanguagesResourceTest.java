package org.magpie.mt.api.service;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.magpie.mt.api.dto.LocaleCode;
import org.magpie.mt.api.service.impl.LanguagesResourceImpl;
import org.magpie.mt.dao.LocaleDAO;
import org.magpie.mt.model.Locale;

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
                Lists.newArrayList(new Locale(LocaleCode.EN_US, "English"),
                        new Locale(LocaleCode.DE, "German"));
        List<org.magpie.mt.api.dto.Locale> expectedLocales = Lists.newArrayList(
                new org.magpie.mt.api.dto.Locale("en-us", "English"),
                new org.magpie.mt.api.dto.Locale("de", "German"));

        when(localeDAO.getSupportedLocales()).thenReturn(locales);

        Response response = languagesResource.getSupportedLanguages();
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        List<org.magpie.mt.api.dto.Locale> dtos = (List)response.getEntity();
        assertThat(dtos).isNotNull().hasSize(locales.size());
        assertThat(dtos.containsAll(expectedLocales)).isTrue();
    }

}
