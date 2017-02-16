package org.zanata.mt.model;

import org.junit.Test;

import java.util.Date;

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

    @Test
    public void testOnPersist() {
        ModelEntityImpl entity = new ModelEntityImpl();
        Date creationDate = entity.getCreationDate();
        Date lastChanged = entity.getLastChanged();

        entity.onPersist();
        assertThat(entity.getCreationDate()).isNotNull()
                .isNotEqualTo(creationDate);
        assertThat(entity.getLastChanged()).isNotNull()
                .isNotEqualTo(lastChanged);
    }

    @Test
    public void testPreUpdate() {
        ModelEntityImpl entity = new ModelEntityImpl();
        Date lastChanged = entity.getLastChanged();

        entity.preUpdate();
        assertThat(entity.getLastChanged()).isNotNull()
                .isNotEqualTo(lastChanged);

    }

    private class ModelEntityImpl extends ModelEntity
    {
        public ModelEntityImpl() {
            super();
        }
    }
}
