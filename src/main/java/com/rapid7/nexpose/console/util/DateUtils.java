package com.rapid7.nexpose.console.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date/time helpers for scan timestamps and report headers.
 *
 * <p>Fixed NEX-3106: {@link #parseScanTimestamp(String)} previously used the
 * pattern {@code yyyy-MM-dd'T'HH:mm:ss'Z'} with a literal quoted {@code 'Z'}.
 * When the scan engine emits an ISO-8601 timestamp with a numeric GMT offset
 * such as {@code 2026-08-04T14:47:59+00:00}, the literal-Z parser threw
 * {@link ParseException} ("unparseable date"). The pattern now uses the
 * {@code XXX} zone designator, which accepts both the literal {@code Z} and
 * numeric offsets such as {@code +00:00}.</p>
 */
public final class DateUtils {

    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

    // Fixed NEX-3106: XXX accepts both 'Z' and numeric offsets like +00:00.
    private static final String SCAN_TS_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

    private DateUtils() {
    }

    public static Date parseScanTimestamp(String value) {
        log.debug("Parsing scan timestamp '{}' with pattern {}", value, SCAN_TS_PATTERN);
        SimpleDateFormat sdf = new SimpleDateFormat(SCAN_TS_PATTERN);
        sdf.setLenient(false);
        try {
            return sdf.parse(value);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unparseable scan timestamp: " + value, e);
        }
    }

    public static String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
