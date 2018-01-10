package org.zanata.magpie.service;

import org.junit.Test;
import org.zanata.magpie.exception.InvalidDateParamException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DateRangeTest {

    @Test
    public void testInvalidRange() {
        String rangeString = "2017-01-01.";
        assertThatThrownBy(() -> DateRange.from(rangeString))
                .isInstanceOf(InvalidDateParamException.class);
    }

    @Test
    public void testNullRange() {
        assertThatThrownBy(() -> DateRange.from(null))
                .isInstanceOf(InvalidDateParamException.class);
    }

    @Test
    public void testRange() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String rangeString = "2017-01-01..2017-08-08";
        DateRange dateRange = DateRange.from(rangeString);
        assertThat(df.format(dateRange.getFromDate())).isEqualTo("2017-01-01");
        assertThat(df.format(dateRange.getToDate())).isEqualTo("2017-08-08");
    }
}
