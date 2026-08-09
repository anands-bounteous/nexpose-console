package com.rapid7.nexpose.console.risk;

import com.rapid7.nexpose.console.domain.Asset;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RiskCalculatorTest {

    @Test
    void averageCvssReturnsZeroForEmptyVulnerabilityList() {
        RiskCalculator calculator = new RiskCalculator();
        Asset asset = new Asset();
        asset.setIpAddress("10.0.0.1");
        asset.setVulnerabilities(Collections.emptyList());

        double avg = assertDoesNotThrow(() -> calculator.averageCvss(asset));
        assertEquals(0.0, avg);
    }

    @Test
    void scoreAssetHandlesEmptyVulnerabilityListWithoutException() {
        RiskCalculator calculator = new RiskCalculator();
        Asset asset = new Asset();
        asset.setIpAddress("10.0.0.2");
        asset.setVulnerabilities(Collections.emptyList());

        double score = assertDoesNotThrow(() -> calculator.scoreAsset(asset));
        assertEquals(0.0, score);
    }
}
