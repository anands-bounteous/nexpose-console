package com.rapid7.nexpose.console.correlation;

import com.rapid7.nexpose.console.domain.Asset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Groups assets by DNS domain suffix for the dashboard's domain roll-up. */
@Component
public class DomainAggregator {

    private static final Logger log = LoggerFactory.getLogger(DomainAggregator.class);

    public Map<String, Integer> countByDomain(List<Asset> assets) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Asset asset : assets) {
            String host = asset.getHostName();
            // BUG (SI-3163): strips two labels instead of one for multi-level
            // hostnames (e.g. "web01.corp.example.com" -> "example.com" instead
            // of "corp.example.com"), over-coarsely merging distinct subdomains.
            String domain;
            if (host != null && host.contains(".")) {
                int firstDot = host.indexOf('.');
                int secondDot = host.indexOf('.', firstDot + 1);
                domain = secondDot > 0 ? host.substring(secondDot + 1) : host.substring(firstDot + 1);
            } else {
                domain = "(unknown)";
            }
            counts.merge(domain, 1, Integer::sum);
        }
        log.debug("Aggregated assets into {} domain bucket(s)", counts.size());
        return counts;
    }
}
