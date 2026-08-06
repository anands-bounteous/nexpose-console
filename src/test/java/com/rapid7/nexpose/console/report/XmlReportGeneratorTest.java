package com.rapid7.nexpose.console.report;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Scan;
import com.rapid7.nexpose.console.domain.ScanStatus;
import com.rapid7.nexpose.console.domain.Vulnerability;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for NEX-3101: XmlReportGenerator must not throw a
 * NullPointerException when an asset is live-but-unfingerprinted and its
 * vulnerability list is null.
 */
class XmlReportGeneratorTest {

    private final XmlReportGenerator generator = new XmlReportGenerator();

    @Test
    void generateHandlesAssetWithNullVulnerabilityListWithoutThrowing() {
        Scan scan = new Scan();
        scan.setName("PCI-Quarterly-External");
        scan.setEngineName("local-engine");
        scan.setStatus(ScanStatus.COMPLETED);

        Asset fingerprinted = new Asset("10.0.0.1", "host1", "Linux");
        fingerprinted.setVulnerabilities(List.of(
                new Vulnerability("ssl-cve-2016-2183", "SWEET32", 7.5, 443, "tcp")));
        scan.getAssets().add(fingerprinted);

        // Live-but-unfingerprinted asset: vulnerabilities list is null.
        Asset unfingerprinted = new Asset("10.0.0.2", "host2", null);
        unfingerprinted.setVulnerabilities(null);
        scan.getAssets().add(unfingerprinted);

        String xml = assertDoesNotThrow(() -> generator.generate(scan));

        assertTrue(xml.contains("ip=\"10.0.0.1\""));
        assertTrue(xml.contains("ip=\"10.0.0.2\""));
        assertTrue(xml.contains("<vulnerabilities count=\"0\">"));
        assertTrue(xml.contains("<vulnerabilities count=\"1\">"));
    }
}
