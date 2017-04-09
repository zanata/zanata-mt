package org.zanata.mt.api.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.mt.api.service.DocumentsResource;
import org.zanata.mt.dao.DocumentDAO;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentsResourceImpl implements DocumentsResource {

    private static final Logger LOG =
            LoggerFactory.getLogger(DocumentsResourceImpl.class);

    private DocumentDAO documentDAO;

    @SuppressWarnings("unused")
    public DocumentsResourceImpl() {
    }

    @Inject
    public DocumentsResourceImpl(DocumentDAO documentDAO) {
        this.documentDAO = documentDAO;
    }

    @Override
    public Response getDocumentUrls() {
        List<String> urls = documentDAO.getUrlList();
        return Response.ok().entity(urls).build();
    }
}
