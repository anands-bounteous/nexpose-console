package com.rapid7.nexpose.console.report;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Scan;
import com.rapid7.nexpose.console.domain.ScanStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for NEX-3101 / SI-2: XML report generation must not fail for
 * a live-but-unfingerprinted asset whose vulnerability list is null.
 */
class XmlReportGeneratorTest {

    @Test
    void generatesReportForAssetWithNullVulnerabilityList() {
        Asset unfingerprinted = new Asset("10.0.0.6", "host-six", "Unknown");
        unfingerprinted.setLive(true);
        unfingerprinted.setVulnerabilities(null); // live-but-unfingerprinted

        Scan scan = new Scan();
        scan.setName("nightly");
        scan.setEngineName("local");
        scan.setStatus(ScanStatus.COMPLETED);
        scan.getAssets().add(unfingerprinted);

        XmlReportGenerator generator = new XmlReportGenerator();

        String xml = assertDoesNotThrow(() -> generator.generate(scan));
        assertTrue(xml.contains("ip=\"10.0.0.6\""), "asset should be serialised");
        assertTrue(xml.contains("<vulnerabilities count=\"0\">"),
                "null vulnerability list should be treated as empty (count=0)");
    }
}
