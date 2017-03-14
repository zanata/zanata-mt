package org.zanata.mt.api.dto;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocaleIdAdapterTest {

    @Test
    public void testUnmarshal() throws Exception {
        LocaleIdAdapter adapter = new LocaleIdAdapter();
        assertThat(adapter.unmarshal(null)).isNull();

        assertThat(adapter.unmarshal("en")).isNotNull().isEqualTo(LocaleId.EN);
    }

    @Test
    public void testMarshal() throws Exception {
        LocaleIdAdapter adapter = new LocaleIdAdapter();
        assertThat(adapter.marshal(null)).isNull();
        assertThat(adapter.marshal(LocaleId.EN)).isNotNull().isEqualTo("en");
    }
}
