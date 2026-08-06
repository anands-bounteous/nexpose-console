package com.rapid7.nexpose.console.report;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Scan;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for NEX-3105: ReportEngine.generateXml() must not throw a
 * ClassCastException when locating the asset section, even though the internal
 * section list places the summary section before the asset section.
 */
class ReportEngineTest {

    @Test
    void generateXmlDoesNotThrowClassCastExceptionWhenSummarySectionIsFirst() {
        Scan scan = new Scan();
        scan.setName("regression-scan");
        scan.setEngineName("mock-engine");

        Asset asset = new Asset("10.0.0.1", "host1", "Linux");
        asset.setVulnerabilities(java.util.Collections.emptyList());
        scan.getAssets().add(asset);

        ReportEngine reportEngine = new ReportEngine(new XmlReportGenerator());

        String xml = assertDoesNotThrow(() -> reportEngine.generateXml(scan),
                "generateXml() should not throw ClassCastException (NEX-3105) even though the "
                        + "summary section is added before the asset section");

        assertTrue(xml.contains("<scan"), "Generated XML should contain the <scan> root element");
        assertTrue(xml.contains("10.0.0.1"), "Generated XML should contain the asset's IP address");
    }
}
