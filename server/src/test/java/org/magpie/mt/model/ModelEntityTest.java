package org.magpie.mt.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ModelEntityTest {

    @Test
    public void testId() {
        ModelEntityImpl entity = new ModelEntityImpl();
        entity.setId(1L);
        assertThat(entity.getId()).isEqualTo(1L);
    }

    private class ModelEntityImpl extends ModelEntity
    {
        public ModelEntityImpl() {
            super();
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }
}
