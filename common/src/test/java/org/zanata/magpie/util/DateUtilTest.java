package org.zanata.magpie.util;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DateUtilTest {

    @Test
    public void testAsDate() {
        String format = "dd-MM-yyyy";
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(format);
        DateFormat df = new SimpleDateFormat(format);

        LocalDate now = LocalDate.now();
        String nowString = now.format(formatter);
        Date nowDate = DateUtil.asDate(now);
        assertThat(nowString).isEqualTo(df.format(nowDate));
    }
}
