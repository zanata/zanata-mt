package org.zanata.magpie.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class DateUtil {
    /**
     * Convert localDate to Date with system default timezone
     */
    public static Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
