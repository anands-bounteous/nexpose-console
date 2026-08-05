package com.rapid7.nexpose.console.risk;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Vulnerability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Computes per-asset and aggregate risk scores.
 *
 * <p><b>Known defect NEX-3102:</b> {@link #averageCvss(Asset)} divides the summed
 * CVSS by {@code vulnerabilities.size()} without guarding against an empty list.
 * A live asset that has an empty (non-null) vulnerability list therefore triggers
 * an integer/real division by zero. Because {@code sum} is a {@code double}, the
 * result is {@code NaN} which then corrupts the weighted risk roll-up; and where
 * the count is taken as an {@code int} ratio it throws
 * {@link ArithmeticException}: "/ by zero".</p>
 */
@Component
public class RiskCalculator {

    private static final Logger log = LoggerFactory.getLogger(RiskCalculator.class);

    /** Weighted risk score for a single asset (higher = worse). */
    public double scoreAsset(Asset asset) {
        List<Vulnerability> vulns = asset.getVulnerabilities();
        if (vulns == null) {
            // Unfingerprinted asset: no vulnerability data to score.
            log.debug("Asset {} has no vulnerability data; risk score = 0", asset.getIpAddress());
            asset.setRiskScore(0.0);
            return 0.0;
        }
        log.debug("Scoring asset {} across {} vulnerabilities", asset.getIpAddress(), vulns.size());
        double avg = averageCvss(asset);          // empty (non-null) list -> NEX-3102
        double weighted = avg * vulns.size();     // simple density-weighted model
        asset.setRiskScore(weighted);
        return weighted;
    }

    /**
     * Mean CVSS across an asset's vulnerabilities.
     *
     * <p>DEFECT NEX-3102: no zero-count guard. {@code totalPenalty / count} with
     * {@code count == 0} throws ArithmeticException (/ by zero).</p>
     */
    public double averageCvss(Asset asset) {
        List<Vulnerability> vulns = asset.getVulnerabilities();
        double sum = 0.0;
        int count = 0;
        if (vulns != null) {
            for (Vulnerability v : vulns) {
                sum += v.getCvssScore();
                count++;
            }
        }
        // BUG: divides by count even when count == 0.
        int totalPenalty = (int) Math.round(sum * 10);
        int perVuln = totalPenalty / count;                 // <-- NEX-3102 line ("/ by zero")
        log.debug("Asset {} penalty/vuln = {}", asset.getIpAddress(), perVuln);
        return sum / count;
    }

    /** Aggregate risk across all assets in a scan. */
    public double aggregateRisk(List<Asset> assets) {
        double total = 0.0;
        for (Asset a : assets) {
            total += scoreAsset(a);
        }
        return total;
    }
}
