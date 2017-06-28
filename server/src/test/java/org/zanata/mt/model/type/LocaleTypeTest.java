package org.zanata.mt.model.type;

import org.hibernate.type.StringType;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleTypeTest {

    @Test
    public void testConstructor() {
        LocaleCodeType type = new LocaleCodeType();
        assertThat(type.getSqlTypeDescriptor())
                .isEqualTo(StringType.INSTANCE.getSqlTypeDescriptor());
        assertThat(type.getJavaTypeDescriptor())
                .isEqualTo(LocaleCodeTypeDescriptor.INSTANCE);
    }

    @Test
    public void testGetName() {
        LocaleCodeType type = new LocaleCodeType();
        assertThat(type.getName()).isEqualTo("localeCode");
    }

    @Test
    public void testObjectToSQLString() throws Exception {
        LocaleCodeType type = new LocaleCodeType();
        String sqlString = type.objectToSQLString(LocaleCode.DE, null);
        assertThat(sqlString).isEqualTo("'de'");
    }
}
