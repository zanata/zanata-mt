package org.zanata.mt.util;

import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;

import org.junit.Before;
import org.junit.Test;
import org.zanata.mt.backend.ms.internal.dto.MSString;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DTOUtilTest {

    private DTOUtil dtoUtil;

    @Before
    public void setUp() {
        dtoUtil = new DTOUtil();
    }

    @Test
    public void testToXML() {
        String expectedXml = "<MSString>testing 123</MSString>";
        MSString obj = new MSString("testing 123");
        String xml = dtoUtil.toXML(obj);
        assertThat(xml).isEqualTo(expectedXml);
    }

    @Test
    public void testToObject() throws JAXBException {
        String xml = "<MSString>testing 123</MSString>";
        MSString expectedObj = new MSString("testing 123");
        MSString obj = dtoUtil.toObject(xml, MSString.class);
        assertThat(obj).isEqualTo(expectedObj);
    }

    @Test
    public void testToObjectInvalid() throws JAXBException {
        String xml = "testing 123";
        assertThatThrownBy(() -> dtoUtil.toObject(xml, MSString.class))
            .isInstanceOf(UnmarshalException.class);
    }

    @Test
    public void testToJson() {
        MSString obj = new MSString("testing 123");
        String expectedJson = "{\"value\":\"testing 123\"}";
        String json = dtoUtil.toJSON(obj);
        assertThat(json).isEqualTo(expectedJson);
    }

    @Test
    public void testFromJsonToObj() throws IOException {
        MSString expectedObj = new MSString("testing 123");
        String json = "{\"value\":\"testing 123\"}";
        MSString obj = dtoUtil.fromJSONToObject(json, MSString.class);
        assertThat(obj).isEqualTo(expectedObj);
    }

    @Test
    public void testFromJsonToObjInvalid() throws IOException {
        String json = "{\"testing 123\"}";
        assertThatThrownBy(() -> dtoUtil.fromJSONToObject(json, MSString.class))
            .isInstanceOf(IOException.class);
    }
}
