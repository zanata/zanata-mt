package org.zanata.mt.model.type;

import org.hibernate.type.StringType;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleTypeTest {

    @Test
    public void testConstructor() {
        LocaleIdType type = new LocaleIdType();
        assertThat(type.getSqlTypeDescriptor())
                .isEqualTo(StringType.INSTANCE.getSqlTypeDescriptor());
        assertThat(type.getJavaTypeDescriptor())
                .isEqualTo(LocaleIdTypeDescriptor.INSTANCE);
    }

    @Test
    public void testGetName() {
        LocaleIdType type = new LocaleIdType();
        assertThat(type.getName()).isEqualTo("localeId");
    }

    @Test
    public void testObjectToSQLString() throws Exception {
        LocaleIdType type = new LocaleIdType();
        String sqlString = type.objectToSQLString(LocaleId.DE, null);
        assertThat(sqlString).isEqualTo("'de'");
    }
}
