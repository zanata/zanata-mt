package org.zanata.mt.service;


import org.apache.commons.lang3.StringUtils;
import org.zanata.mt.exception.InvalidDateParamException;
import org.zanata.mt.util.DateUtil;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DateRange {
    private final LocalDate fromDate;
    private final LocalDate toDate;

    private final static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateRange(@NotNull final LocalDate fromDate,
            @NotNull final LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public static DateRange from(String dateRangeParam) {
        if (StringUtils.isBlank(dateRangeParam)) {
            throw new InvalidDateParamException(
                    "Empty data range");
        }
        String[] dateRange = dateRangeParam.split("\\.\\.");
        if (dateRange.length != 2) {
            throw new InvalidDateParamException(
                    "Invalid data range: " + dateRangeParam);
        }

        LocalDate fromDate;
        LocalDate toDate;
        try {
            fromDate = LocalDate.parse(dateRange[0], formatter);
            toDate = LocalDate.parse(dateRange[1], formatter);
        } catch (IllegalArgumentException e) {
            throw new InvalidDateParamException(
                    "Invalid data range: " + dateRangeParam);
        }
        return new DateRange(fromDate, toDate);
    }

    public Date getFromDate() {
        return DateUtil.asDate(fromDate);
    }

    public Date getToDate() {
        return DateUtil.asDate(toDate);
    }
}
