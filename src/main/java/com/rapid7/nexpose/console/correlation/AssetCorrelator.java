package com.rapid7.nexpose.console.correlation;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.exception.CorrelationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Correlates raw scan output into a de-duplicated asset set. Two assets are
 * considered the same host if they share an IP address (e.g. the same host seen
 * by two engines, or discovered twice in overlapping CIDR ranges).
 */
@Component
public class AssetCorrelator {

    private static final Logger log = LoggerFactory.getLogger(AssetCorrelator.class);

    public List<Asset> mergeDuplicates(List<Asset> assets) {
        log.info("Correlating {} raw asset record(s)", assets.size());
        java.util.Set<String> seen = new java.util.HashSet<>();
        List<Asset> result = new ArrayList<>();
        try {
            for (Asset asset : assets) {
                String ip = asset.getIpAddress();
                if (seen.contains(ip)) {
                    log.debug("Duplicate asset {} - skipping", ip);
                } else {
                    seen.add(ip);
                    result.add(asset);
                }
            }
        } catch (RuntimeException e) {
            throw new CorrelationException("Failed to correlate assets during de-duplication", e);
        }
        log.info("Correlation produced {} unique asset(s)", result.size());
        return result;
    }
}
