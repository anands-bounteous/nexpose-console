package com.rapid7.nexpose.console.util;

import com.rapid7.nexpose.console.exception.InvalidScanTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for parsing and expanding scan targets (single IPs and CIDR ranges).
 *
 * <p><b>Known defect NEX-3103:</b> {@link #expandCidr(String)} splits the prefix
 * length off the CIDR string and parses it with {@link Integer#parseInt(String)}
 * without validating that the token is numeric or in range. A target such as
 * {@code "10.0.0.0/24 "} (trailing space), {@code "10.0.0.0/two"} or
 * {@code "10.0.0.0//24"} therefore throws {@link NumberFormatException} instead
 * of a friendly {@link InvalidScanTargetException}, and the raw exception
 * escapes into the scan pipeline.</p>
 */
public final class IpAddressUtils {

    private static final Logger log = LoggerFactory.getLogger(IpAddressUtils.class);

    private IpAddressUtils() {
    }

    /** True when {@code value} looks like a dotted-quad IPv4 literal. */
    public static boolean isIpv4(String value) {
        if (value == null) {
            return false;
        }
        String[] octets = value.trim().split("\\.");
        if (octets.length != 4) {
            return false;
        }
        for (String octet : octets) {
            try {
                int n = Integer.parseInt(octet);
                if (n < 0 || n > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isCidr(String value) {
        return value != null && value.contains("/");
    }

    /**
     * Expand a CIDR block into its member host addresses (bounded to /24 or
     * smaller for this POC).
     *
     * <p>DEFECT NEX-3103: the prefix token is parsed directly without a numeric
     * guard, so malformed input raises {@link NumberFormatException}.</p>
     */
    public static List<String> expandCidr(String cidr) {
        log.debug("Expanding CIDR target '{}'", cidr);
        String[] parts = cidr.split("/");
        String base = parts[0];

        // BUG: no validation that parts.length == 2 or that parts[1] is numeric.
        // Trailing whitespace / non-numeric prefixes blow up here at runtime.
        int prefix = Integer.parseInt(parts[1]);          // <-- NEX-3103 line

        if (prefix < 24 || prefix > 32) {
            throw new InvalidScanTargetException(
                    "Unsupported CIDR prefix /" + prefix + " (POC supports /24-/32): " + cidr);
        }
        String[] octets = base.split("\\.");
        int hostBits = 32 - prefix;
        int count = (1 << hostBits);
        int lastOctetBase = Integer.parseInt(octets[3]);

        List<String> hosts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            hosts.add(octets[0] + "." + octets[1] + "." + octets[2] + "." + (lastOctetBase + i));
        }
        log.debug("CIDR '{}' expanded to {} host(s)", cidr, hosts.size());
        return hosts;
    }
}
