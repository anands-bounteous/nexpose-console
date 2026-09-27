package com.rapid7.nexpose.console.util;

import com.rapid7.nexpose.console.exception.InvalidScanTargetException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IpAddressUtilsTest {

    @Test
    void expandCidr_withValidPrefix_returnsExpectedHosts() {
        List<String> hosts = IpAddressUtils.expandCidr("192.168.10.0/30");
        assertEquals(4, hosts.size());
        assertEquals("192.168.10.0", hosts.get(0));
        assertEquals("192.168.10.3", hosts.get(3));
    }

    @Test
    void expandCidr_withNonNumericPrefix_throwsInvalidScanTargetException_notNumberFormatException() {
        // NEX-3103: previously this threw a raw NumberFormatException.
        InvalidScanTargetException ex = assertThrows(InvalidScanTargetException.class,
                () -> IpAddressUtils.expandCidr("10.0.0.0/2a"));
        assertEquals("NEXL-SCAN-001", ex.getErrorCode());
    }

    @Test
    void expandCidr_withTrailingWhitespaceInPrefix_throwsInvalidScanTargetException() {
        assertThrows(InvalidScanTargetException.class,
                () -> IpAddressUtils.expandCidr("10.0.0.0/24 "));
    }

    @Test
    void expandCidr_withExtraSlash_throwsInvalidScanTargetException() {
        assertThrows(InvalidScanTargetException.class,
                () -> IpAddressUtils.expandCidr("10.0.0.0//24"));
    }

    @Test
    void expandCidr_withMissingSlash_throwsInvalidScanTargetException() {
        assertThrows(InvalidScanTargetException.class,
                () -> IpAddressUtils.expandCidr("10.0.0.0"));
    }
}
