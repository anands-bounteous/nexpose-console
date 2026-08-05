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
                hosts.add(target);
            }
        }
        log.info("Resolved {} target expression(s) into {} host(s)", targets.size(), hosts.size());
        return hosts;
    }
}
