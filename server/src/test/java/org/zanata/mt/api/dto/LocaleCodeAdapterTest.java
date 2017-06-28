package org.zanata.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleCodeAdapterTest {

    @Test
    public void testUnmarshal() throws Exception {
        LocaleCodeAdapter adapter = new LocaleCodeAdapter();
        assertThat(adapter.unmarshal(null)).isNull();

        assertThat(adapter.unmarshal("en")).isNotNull().isEqualTo(LocaleCode.EN);
    }

    @Test
    public void testMarshal() throws Exception {
        LocaleCodeAdapter adapter = new LocaleCodeAdapter();
        assertThat(adapter.marshal(null)).isNull();
        assertThat(adapter.marshal(LocaleCode.EN)).isNotNull().isEqualTo("en");
    }
}
