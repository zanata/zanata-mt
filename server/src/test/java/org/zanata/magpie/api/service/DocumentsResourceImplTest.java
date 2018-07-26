package org.zanata.magpie.api.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.magpie.api.service.impl.DocumentsResourceImpl;
import org.zanata.magpie.dao.DocumentDAO;
import org.zanata.magpie.dto.DateRange;

import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentsResourceImplTest {

    private DocumentsResource documentsResource;

    @Mock
    private DocumentDAO documentDAO;

    @Before
    public void beforeTest() {
        documentsResource = new DocumentsResourceImpl(documentDAO);
    }

    @Test
    public void testConstructor() {
        DocumentsResource
                resource = new DocumentsResourceImpl();
    }

    @Test
    public void testGetDocumentUrls() {
        Optional<DateRange> dateParam = Optional.empty();

        List<String> urls =
                ImmutableList.of("http://locale", "http://locale2",
                        "http://locale3");
        when(documentDAO.getUrlList(dateParam)).thenReturn(urls);
        Response response = documentsResource.getDocumentUrls(null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        List<String> returnedUrl = (List<String>)response.getEntity();
        assertThat(returnedUrl).containsAll(urls);
    }
}
