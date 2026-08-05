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
 * <p><b>Known defect NEX-3104:</b> {@link #mergeDuplicates(List)} removes elements
 * from the very list it is iterating with an enhanced for-loop. As soon as the
 * scan contains a duplicate IP, the removal mutates the backing list mid-iteration
 * and the next {@code iterator.next()} throws
 * {@link java.util.ConcurrentModificationException}. The correct approach is to
 * iterate with an explicit {@link java.util.Iterator} and call
 * {@code iterator.remove()}, or collect survivors into a new list.</p>
 */
@Component
public class AssetCorrelator {

    private static final Logger log = LoggerFactory.getLogger(AssetCorrelator.class);

    public List<Asset> mergeDuplicates(List<Asset> assets) {
        log.info("Correlating {} raw asset record(s)", assets.size());
        java.util.Set<String> seen = new java.util.HashSet<>();
        try {
            // BUG NEX-3104: structural modification of `assets` during for-each.
            for (Asset asset : assets) {                       // <-- CME thrown on .next()
                String ip = asset.getIpAddress();
                if (seen.contains(ip)) {
                    log.debug("Duplicate asset {} - removing during iteration", ip);
                    assets.remove(asset);                      // <-- NEX-3104 line
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
