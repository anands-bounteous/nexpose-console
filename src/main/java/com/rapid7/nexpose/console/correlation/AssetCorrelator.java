package com.rapid7.nexpose.console.correlation;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.exception.CorrelationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Correlates raw scan output into a de-duplicated asset set. Two assets are
 * considered the same host if they share an IP address (e.g. the same host seen
 * by two engines, or discovered twice in overlapping CIDR ranges).
 *
 * <p>De-duplication removes repeated IPs via an explicit {@link java.util.Iterator}
 * and {@link java.util.Iterator#remove()}, so the backing list is not structurally
 * modified mid-iteration. This fixes NEX-3104, where removing directly from the
 * list inside an enhanced for-loop threw
 * {@link java.util.ConcurrentModificationException} as soon as a scan contained a
 * duplicate IP.</p>
 */
@Component
public class AssetCorrelator {

    private static final Logger log = LoggerFactory.getLogger(AssetCorrelator.class);

    public List<Asset> mergeDuplicates(List<Asset> assets) {
        log.info("Correlating {} raw asset record(s)", assets.size());
        java.util.Set<String> seen = new java.util.HashSet<>();
        try {
            // NEX-3104: remove through the Iterator so the backing list is not
            // structurally modified mid-iteration (which threw a
            // ConcurrentModificationException on the next Iterator.next()).
            for (java.util.Iterator<Asset> it = assets.iterator(); it.hasNext(); ) {
                Asset asset = it.next();
                String ip = asset.getIpAddress();
                if (seen.contains(ip)) {
                    log.debug("Duplicate asset {} - removing during iteration", ip);
                    it.remove();
                } else {
                    seen.add(ip);
                }
            }
        } catch (RuntimeException e) {
            throw new CorrelationException("Failed to correlate assets during de-duplication", e);
        }
        log.info("Correlation produced {} unique asset(s)", assets.size());
        return assets;
    }
}
