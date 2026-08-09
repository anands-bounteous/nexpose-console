package com.rapid7.nexpose.console.risk;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Severity;
import com.rapid7.nexpose.console.domain.Vulnerability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Computes per-asset and aggregate risk scores.
 *
 * <p>NEX-3102 (fixed): {@link #averageCvss(Asset)} previously divided the summed
 * CVSS by {@code vulnerabilities.size()} without guarding against an empty list,
 * which threw {@link ArithmeticException} for a fingerprinted asset with zero
 * vulnerabilities. It now returns {@code 0.0} for that case.</p>
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
        double avg = averageCvss(asset);          // empty (non-null) list -> 0.0, no exception
        double weighted = avg * vulns.size();     // simple density-weighted model
        // Scale by the tier's severity weight (see Severity.weight()).
        // BUG (SI-3161): SEVERE's weight constant (2) is out of line with the
        // rest of the table (CRITICAL=10, MODERATE=4), so hosts whose average
        // CVSS falls in the SEVERE band score *lower* than MODERATE ones.
        weighted = weighted * (Severity.fromCvss(avg).weight() / 10.0);
        asset.setRiskScore(weighted);
        return weighted;
    }

    /**
     * Mean CVSS across an asset's vulnerabilities.
     *
     * <p>NEX-3102 (fixed): guards against a zero vulnerability count, which
     * previously caused {@code totalPenalty / count} to throw
     * ArithmeticException (/ by zero).</p>
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
        if (count == 0) {
            // No vulnerabilities to average -> baseline score, no division.
            return 0.0;
        }
        int totalPenalty = (int) Math.round(sum * 10);
        int perVuln = totalPenalty / count;
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
