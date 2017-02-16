package org.zanata.mt.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendIDTest {

    @Test
    public void testConstructor() {
        BackendID id = new BackendID("id1");
        assertThat(id.getId()).isEqualTo("id1");
    }

    @Test
    public void testToString() {
        BackendID id = new BackendID("id1");
        assertThat(id.toString()).isEqualTo("id1");
    }

    @Test
    public void testEqualsAndHashcode() {
        BackendID id1 = new BackendID("id1");
        BackendID id2 = new BackendID("id1");

        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
        assertThat(id1.equals(id2)).isTrue();

        id2 = new BackendID("id2");
        assertThat(id1.hashCode()).isNotEqualTo(id2.hashCode());
        assertThat(id1.equals(id2)).isFalse();

        // diff type
        String test = "test";
        assertThat(id1.hashCode()).isNotEqualTo(test.hashCode());
        assertThat(id1.equals(test)).isFalse();
    }
}
