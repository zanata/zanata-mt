package org.zanata.mt.model.type;

import org.hibernate.HibernateException;
import org.junit.Test;
import org.zanata.mt.api.dto.LocaleId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleTypeDescriptorTest {

    @Test
    public void testConstructor() {
        LocaleIdTypeDescriptor descriptor = new LocaleIdTypeDescriptor();
        assertThat(descriptor.getJavaTypeClass()).isEqualTo(LocaleId.class);
    }

    @Test
    public void testFromString() {
        LocaleIdTypeDescriptor descriptor = new LocaleIdTypeDescriptor();
        assertThat(descriptor.fromString(null)).isNull();
        assertThat(descriptor.fromString("en")).isEqualTo(LocaleId.EN);
    }

    @Test
    public void testToString() {
        LocaleIdTypeDescriptor descriptor = new LocaleIdTypeDescriptor();
        assertThat(descriptor.toString(LocaleId.DE)).isEqualTo("de");
    }

    @Test
    public void testUnwrap() {
        LocaleIdTypeDescriptor descriptor = new LocaleIdTypeDescriptor();
        assertThat(descriptor.unwrap(null, LocaleId.class, null)).isNull();

        assertThat(descriptor.unwrap(LocaleId.EN, String.class, null))
                .isEqualTo("en");

        assertThatThrownBy(
                () -> descriptor.unwrap(LocaleId.EN, Integer.class, null))
                .isInstanceOf(HibernateException.class);
    }

    @Test
    public void testWrap() {
        LocaleIdTypeDescriptor descriptor = new LocaleIdTypeDescriptor();
        assertThat(descriptor.wrap(null, null)).isNull();

        assertThat(descriptor.wrap("EN", null)).isEqualTo(LocaleId.EN);

        assertThatThrownBy(
                () -> descriptor.wrap(1, null))
                .isInstanceOf(HibernateException.class);
    }
}
