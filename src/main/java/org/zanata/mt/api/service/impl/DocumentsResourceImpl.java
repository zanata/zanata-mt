package org.zanata.mt.api.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.api.service.DocumentsResource;
import org.zanata.mt.dao.DocumentDAO;
import org.zanata.mt.service.DateRange;

import javax.inject.Inject;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentsResourceImpl implements DocumentsResource {
    private DocumentDAO documentDAO;

    @SuppressWarnings("unused")
    public DocumentsResourceImpl() {
    }

    @Inject
    public DocumentsResourceImpl(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    @Override
    public Response getDocumentUrls(
            @QueryParam("dateRange") String dateRangeParam) {
        Optional<DateRange> dateParam =
                StringUtils.isBlank(dateRangeParam) ? Optional.empty() :
                        Optional.of(DateRange.from(dateRangeParam));

        List<String> urls = documentDAO.getUrlList(dateParam);
        return Response.ok().entity(urls).build();
    }
}
