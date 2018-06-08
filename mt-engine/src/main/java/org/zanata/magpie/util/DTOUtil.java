package org.zanata.magpie.util;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class DTOUtil {
    private final static Logger LOG = LoggerFactory.getLogger(DTOUtil.class);

    public String toJSON(Object obj) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (IOException e) {
            LOG.error("toJSON failed", e);
            return obj.getClass().getName() + "@"
                    + Integer.toHexString(obj.hashCode());
        }
    }

    public <T> T fromJSONToObject(String json, Class<T> clazz)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, clazz);
    }

    public <T> T fromJSONToObjectList(String json, TypeReference<T> typeReference)
            throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, typeReference);
    }
}
