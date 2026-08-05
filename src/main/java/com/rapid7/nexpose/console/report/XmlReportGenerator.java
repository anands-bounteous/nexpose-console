package com.rapid7.nexpose.console.report;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.Scan;
import com.rapid7.nexpose.console.domain.Vulnerability;
import com.rapid7.nexpose.console.exception.ReportGenerationException;
import com.rapid7.nexpose.console.util.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generates the XML scan report: a {@code <scan>} document listing every asset
 * and, nested under each asset, its vulnerabilities.
 *
 * <p><b>Known defect NEX-3101 (code fix):</b> {@link #appendAsset(StringBuilder, Asset)}
 * iterates {@code asset.getVulnerabilities()} with a for-each loop without first
 * checking for {@code null}. The {@link com.rapid7.nexpose.console.scan.MockScanEngine}
 * legitimately returns live-but-unfingerprinted assets whose vulnerability list is
 * {@code null}, so report generation throws {@link NullPointerException}
 * ("Cannot invoke ... because the return value of ...getVulnerabilities() is null")
 * and the whole report fails. The fix is a null-guard (treat null as empty).</p>
 */
@Component
public class XmlReportGenerator {

    private static final Logger log = LoggerFactory.getLogger(XmlReportGenerator.class);

    public String generate(Scan scan) {
        log.info("Generating XML report for scan '{}' ({} asset(s))",
                scan.getName(), scan.getAssets().size());
        try {
            StringBuilder xml = new StringBuilder(4096);
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<scan name=\"").append(XmlUtils.escape(scan.getName())).append("\" ")
               .append("engine=\"").append(XmlUtils.escape(scan.getEngineName())).append("\" ")
               .append("status=\"").append(scan.getStatus()).append("\">\n");
            xml.append("  <assets>\n");
            for (Asset asset : scan.getAssets()) {
                appendAsset(xml, asset);
            }
            xml.append("  </assets>\n");
            xml.append("</scan>\n");
            log.info("XML report for scan '{}' generated ({} chars)", scan.getName(), xml.length());
            return xml.toString();
        } catch (ReportGenerationException e) {
            throw e;
        } catch (RuntimeException e) {
            // Wrap unexpected failures with the report error code, but the NPE
            // below is raised inside appendAsset and rethrown here for context.
            throw new ReportGenerationException(
                    "Failed to generate XML report for scan '" + scan.getName() + "'", e);
        }
    }

    private void appendAsset(StringBuilder xml, Asset asset) {
        log.debug("Serialising asset {} to XML", asset.getIpAddress());
        xml.append("    <asset ip=\"").append(XmlUtils.escape(asset.getIpAddress())).append("\" ")
           .append("host=\"").append(XmlUtils.escape(asset.getHostName())).append("\" ")
           .append("os=\"").append(XmlUtils.escape(asset.getOperatingSystem())).append("\">\n");
        xml.append("      <vulnerabilities count=\"")
           .append(asset.getVulnerabilities().size())          // <-- NEX-3101 (null list)
           .append("\">\n");

        // BUG NEX-3101: no null check; a null vulnerability list NPEs on the
        // for-each above (size()) and here.
        for (Vulnerability v : asset.getVulnerabilities()) {
            appendVulnerability(xml, v);
        }
        xml.append("      </vulnerabilities>\n");
        xml.append("    </asset>\n");
    }

    private void appendVulnerability(StringBuilder xml, Vulnerability v) {
        xml.append("        <vulnerability id=\"").append(XmlUtils.escape(v.getId())).append("\" ")
           .append("cvss=\"").append(v.getCvssScore()).append("\" ")
           .append("severity=\"").append(v.getSeverity()).append("\" ")
           .append("port=\"").append(v.getPort()).append("\" ")
           .append("protocol=\"").append(XmlUtils.escape(v.getProtocol())).append("\">")
           .append(XmlUtils.escape(v.getTitle()))
           .append("</vulnerability>\n");
    }
}
