package org.zanata.mt.model;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class BackendIDTest {

    @Test
    public void getFromString() {
        assertThat(BackendID.fromString("dev")).isEqualTo(BackendID.DEV);
        assertThat(BackendID.fromString("M")).isEqualTo(BackendID.MS);
        assertThat(BackendID.fromString("Ms")).isEqualTo(BackendID.MS);
        assertThat(BackendID.fromString("ms")).isEqualTo(BackendID.MS);
        assertThat(BackendID.fromString("g")).isEqualTo(BackendID.GOOGLE);
        assertThat(BackendID.fromString("google")).isEqualTo(BackendID.GOOGLE);
        assertThat(BackendID.fromString("")).isNull();
        assertThat(BackendID.fromString(null)).isNull();
        assertThatThrownBy(() -> BackendID.fromString("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("can not parse [unknown] to a BackendID");
    }
}
