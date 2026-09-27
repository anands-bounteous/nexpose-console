package com.rapid7.nexpose.console.util;

import com.rapid7.nexpose.console.exception.InvalidScanTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for parsing and expanding scan targets (single IPs and CIDR ranges).
 *
 * <p>{@link #expandCidr(String)} validates the CIDR string before parsing the
 * prefix length, so malformed input (trailing whitespace, non-numeric prefix,
 * missing/extra '/' separators) raises a friendly
 * {@link InvalidScanTargetException} instead of letting a raw
 * {@link NumberFormatException} escape into the scan pipeline (NEX-3103).</p>
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
     * <p>The prefix token is validated to be a plain, non-negative integer
     * before parsing. Any malformed CIDR (e.g. trailing whitespace, extra
     * '/' characters, or a non-numeric prefix such as "10.0.0.0/2a") results
     * in an {@link InvalidScanTargetException} rather than an unchecked
     * {@link NumberFormatException}.</p>
     */
    public static List<String> expandCidr(String cidr) {
        log.debug("Expanding CIDR target '{}'", cidr);

        if (cidr == null) {
            throw new InvalidScanTargetException("CIDR target must not be null");
        }

        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new InvalidScanTargetException("Malformed CIDR target: " + cidr);
        }

        String base = parts[0];
        String prefixToken = parts[1];

        if (!prefixToken.matches("\\d+")) {
            throw new InvalidScanTargetException("Malformed CIDR prefix in target: " + cidr);
        }

        int prefix;
        try {
            prefix = Integer.parseInt(prefixToken);
        } catch (NumberFormatException e) {
            throw new InvalidScanTargetException("Malformed CIDR prefix in target: " + cidr, e);
        }

        if (prefix < 24 || prefix > 32) {
            throw new InvalidScanTargetException(
                    "Unsupported CIDR prefix /" + prefix + " (POC supports /24-/32): " + cidr);
        }

        String[] octets = base.split("\\.");
        if (octets.length != 4) {
            throw new InvalidScanTargetException("Malformed CIDR base address in target: " + cidr);
        }

        int lastOctetBase;
        try {
            lastOctetBase = Integer.parseInt(octets[3]);
        } catch (NumberFormatException e) {
            throw new InvalidScanTargetException("Malformed CIDR base address in target: " + cidr, e);
        }

        int hostBits = 32 - prefix;
        int count = (1 << hostBits);

        List<String> hosts = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            hosts.add(octets[0] + "." + octets[1] + "." + octets[2] + "." + (lastOctetBase + i));
        }
        log.debug("CIDR '{}' expanded to {} host(s)", cidr, hosts.size());
        return hosts;
    }
}
