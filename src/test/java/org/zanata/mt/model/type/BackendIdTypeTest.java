package org.zanata.mt.model.type;

import org.hibernate.MappingException;
import org.hibernate.type.StringType;
import org.junit.Test;
import org.zanata.mt.exception.ZanataMTException;
import org.zanata.mt.model.BackendID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendIdTypeTest {

    @Test
    public void testConstructor() {
        BackendIdType type = new BackendIdType();
        assertThat(type.getSqlTypeDescriptor())
                .isEqualTo(StringType.INSTANCE.getSqlTypeDescriptor());
        assertThat(type.getJavaTypeDescriptor())
                .isEqualTo(BackendIdTypeDescriptor.INSTANCE);
    }

    @Test
    public void testToString() {
        BackendIdType type = new BackendIdType();
        assertThat(type.toString(BackendID.MS)).isNotNull()
                .isEqualTo(BackendID.MS.getId());
    }

    @Test
    public void testGetName() {
        BackendIdType type = new BackendIdType();
        assertThat(type.getName()).isEqualTo("backendID");
    }

    @Test
    public void testObjectToSQLString() throws Exception {
        BackendIdType type = new BackendIdType();
        String sqlString = type.objectToSQLString(BackendID.MS, null);
        assertThat(sqlString).isEqualTo("'MS'");
    }

    @Test
    public void testStringToObjectNull() throws Exception {
        BackendIdType type = new BackendIdType();
        assertThatThrownBy(() -> type.stringToObject(null))
                .isInstanceOf(MappingException.class);

    }

    @Test
    public void testStringToObject() throws Exception {
        BackendIdType type = new BackendIdType();
        BackendID id = type.stringToObject("MS");
        assertThat(id).isEqualTo(BackendID.MS);
    }

    @Test
    public void testFromStringValue() throws Exception {
        BackendIdType type = new BackendIdType();
        BackendID id = type.fromStringValue("MS");
        assertThat(id).isEqualTo(BackendID.MS);
    }
}
