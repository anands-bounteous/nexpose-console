package com.rapid7.nexpose.console.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date/time helpers for scan timestamps and report headers.
 *
 * <p><b>Known defect NEX-3106:</b> {@link #parseScanTimestamp(String)} uses the
 * pattern {@code yyyy-MM-dd'T'HH:mm:ss'Z'} with a literal quoted {@code 'Z'}.
 * When the scan engine emits an ISO-8601 timestamp with a numeric GMT offset
 * such as {@code 2026-08-04T14:47:59+00:00}, the literal-Z parser throws
 * {@link ParseException} ("unparseable date"). This mirrors the real
 * GMT+00:00 timezone handling bug.</p>
 */
public final class DateUtils {

    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

    // BUG NEX-3106: 'Z' is a *literal* here, not the RFC-822/ISO zone token.
    private static final String SCAN_TS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private DateUtils() {
    }

    public static Date parseScanTimestamp(String value) {
        log.debug("Parsing scan timestamp '{}' with pattern {}", value, SCAN_TS_PATTERN);
        SimpleDateFormat sdf = new SimpleDateFormat(SCAN_TS_PATTERN);
        sdf.setLenient(false);
        try {
            return sdf.parse(value);                       // <-- NEX-3106 line
        } catch (ParseException e) {
            // The offset form (+00:00) never matches the literal 'Z' pattern.
            throw new IllegalArgumentException("Unparseable scan timestamp: " + value, e);
        }
    }

    public static String format(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}
