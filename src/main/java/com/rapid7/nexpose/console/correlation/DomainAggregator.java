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
            String domain = (host != null && host.contains("."))
                    ? host.substring(host.indexOf('.') + 1)
                    : "(unknown)";
            counts.merge(domain, 1, Integer::sum);
        }
        log.debug("Aggregated assets into {} domain bucket(s)", counts.size());
        return counts;
    }
}
