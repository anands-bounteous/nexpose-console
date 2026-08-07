package com.rapid7.nexpose.console.util;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DateUtilsTest {

    @Test
    void parsesTimestampWithNumericOffset() {
        Date date = DateUtils.parseScanTimestamp("2026-08-04T14:47:59+00:00");
        assertNotNull(date);
    }

    @Test
    void parsesTimestampWithLiteralZ() {
        Date date = DateUtils.parseScanTimestamp("2026-08-04T14:47:59Z");
        assertNotNull(date);
    }
}
