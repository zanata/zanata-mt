/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.magpie.api.service.impl;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.zanata.magpie.api.service.DocumentsResource;
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
                Lists.newArrayList("http://locale", "http://locale2",
                        "http://locale3");
        when(documentDAO.getUrlList(dateParam)).thenReturn(urls);
        Response response = documentsResource.getDocumentUrls(null);
        assertThat(response.getStatus())
                .isEqualTo(Response.Status.OK.getStatusCode());
        List<String> returnedUrl = (List<String>)response.getEntity();
        assertThat(returnedUrl).containsAll(urls);
    }
}
