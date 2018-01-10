package org.magpie.mt.model.type;

import org.hibernate.HibernateException;
import org.junit.Test;
import org.magpie.mt.api.dto.LocaleCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleTypeDescriptorTest {

    @Test
    public void testConstructor() {
        LocaleCodeTypeDescriptor descriptor = new LocaleCodeTypeDescriptor();
        assertThat(descriptor.getJavaTypeClass()).isEqualTo(LocaleCode.class);
    }

    @Test
    public void testFromString() {
        LocaleCodeTypeDescriptor descriptor = new LocaleCodeTypeDescriptor();
        assertThat(descriptor.fromString(null)).isNull();
        assertThat(descriptor.fromString("en")).isEqualTo(LocaleCode.EN);
    }

    @Test
    public void testToString() {
        LocaleCodeTypeDescriptor descriptor = new LocaleCodeTypeDescriptor();
        assertThat(descriptor.toString(LocaleCode.DE)).isEqualTo("de");
    }

    @Test
    public void testUnwrap() {
        LocaleCodeTypeDescriptor descriptor = new LocaleCodeTypeDescriptor();
        assertThat(descriptor.unwrap(null, LocaleCode.class, null)).isNull();

        assertThat(descriptor.unwrap(LocaleCode.EN, String.class, null))
                .isEqualTo("en");

        assertThatThrownBy(
                () -> descriptor.unwrap(LocaleCode.EN, Integer.class, null))
                .isInstanceOf(HibernateException.class);
    }

    @Test
    public void testWrap() {
        LocaleCodeTypeDescriptor descriptor = new LocaleCodeTypeDescriptor();
        assertThat(descriptor.wrap(null, null)).isNull();

        assertThat(descriptor.wrap("EN", null)).isEqualTo(LocaleCode.EN);

        assertThatThrownBy(
                () -> descriptor.wrap(1, null))
                .isInstanceOf(HibernateException.class);
    }
}
