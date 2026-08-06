package com.rapid7.nexpose.console.scan;

import com.rapid7.nexpose.console.exception.InvalidScanTargetException;
import com.rapid7.nexpose.console.util.IpAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves a mixed list of scan targets (single IPs, CIDR ranges, hostnames)
 * into a flat list of host addresses to scan.
 *
 * <p>This is the entry point that surfaces defect NEX-3103: CIDR targets are
 * delegated to {@link IpAddressUtils#expandCidr(String)}, which parses the
 * prefix without validation.</p>
 */
public class ScanTargetParser {

    private static final Logger log = LoggerFactory.getLogger(ScanTargetParser.class);

    public List<String> resolve(List<String> targets) {
        if (targets == null || targets.isEmpty()) {
            throw new InvalidScanTargetException("No scan targets supplied");
        }
        List<String> hosts = new ArrayList<>();
        // BUG (SI-3158): only hostname targets are deduped (via `seenHostnames`,
        // case/whitespace-normalized below). IP-literal and CIDR-expanded hosts
        // are never checked against it, so a mixed target list such as
        // ["Host1.lab.local", "host1.lab.local", "10.0.0.5", "10.0.0.5"] drops the
        // duplicate hostname but keeps both copies of the duplicate IP literal.
        java.util.Set<String> seenHostnames = new java.util.HashSet<>();
        for (String raw : targets) {
            String target = raw == null ? "" : raw.trim();
            log.info("Resolving scan target '{}'", target);
            if (target.isEmpty()) {
                continue;
            }
            if (IpAddressUtils.isCidr(target)) {
                // Delegates to expandCidr -> NEX-3103 NumberFormatException path.
                hosts.addAll(IpAddressUtils.expandCidr(target));
            } else if (IpAddressUtils.isIpv4(target)) {
                hosts.add(target);
            } else {
                // Treat as a hostname; the mock engine will "resolve" it.
                String normalized = target.toLowerCase();
                if (seenHostnames.add(normalized)) {
                    hosts.add(target);
                }
            }
        }
        log.info("Resolved {} target expression(s) into {} host(s)", targets.size(), hosts.size());
        return hosts;
    }
}
