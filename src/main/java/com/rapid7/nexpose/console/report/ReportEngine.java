package com.rapid7.nexpose.console.report;

import com.rapid7.nexpose.console.domain.Asset;
import com.rapid7.nexpose.console.domain.AssetReportSection;
import com.rapid7.nexpose.console.domain.ReportSection;
import com.rapid7.nexpose.console.domain.Scan;
import com.rapid7.nexpose.console.domain.SummaryReportSection;
import com.rapid7.nexpose.console.exception.ReportGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates report assembly: it builds an ordered list of {@link ReportSection}s
 * (a summary section plus an asset section) and hands them to a format-specific
 * generator.
 *
 * <p><b>Known defect NEX-3105 (code fix):</b> {@link #buildSections(Scan)} returns
 * a heterogeneous list containing both a {@link SummaryReportSection} and an
 * {@link AssetReportSection}, but {@link #assetSectionOf(List)} blindly casts the
 * <em>first</em> element to {@link AssetReportSection}. Because the summary section
 * is added first, the cast throws {@link ClassCastException}
 * ("class SummaryReportSection cannot be cast to class AssetReportSection").</p>
 */
@Component
public class ReportEngine {

    private static final Logger log = LoggerFactory.getLogger(ReportEngine.class);

    private final XmlReportGenerator xmlReportGenerator;

    @Autowired
    public ReportEngine(XmlReportGenerator xmlReportGenerator) {
        this.xmlReportGenerator = xmlReportGenerator;
    }

    public String generateXml(Scan scan) {
        log.info("ReportEngine assembling XML report for scan '{}'", scan.getName());
        List<ReportSection> sections = buildSections(scan);

        // Touch the asset section (e.g. for validation/telemetry) before rendering.
        AssetReportSection assetSection = assetSectionOf(sections);
        log.debug("Report has {} section(s); asset section carries {} asset(s)",
                sections.size(), assetSection.getAssets().size());

        return xmlReportGenerator.generate(scan);
    }

    private List<ReportSection> buildSections(Scan scan) {
        List<ReportSection> sections = new ArrayList<>();
        // Summary is intentionally added FIRST.
        sections.add(new SummaryReportSection(
                scan.getAssets().size(), scan.totalVulnerabilities(), 0.0));
        sections.add(new AssetReportSection(scan.getAssets()));
        return sections;
    }

    /**
     * DEFECT NEX-3105: assumes element 0 is the asset section and casts it.
     * The summary section is element 0, so this throws ClassCastException.
     */
    private AssetReportSection assetSectionOf(List<ReportSection> sections) {
        try {
            return (AssetReportSection) sections.get(0);         // <-- NEX-3105 line
        } catch (ClassCastException e) {
            throw new ReportGenerationException(
                    "Report section layout mismatch while locating the asset section", e);
        }
    }

    @SuppressWarnings("unused")
    private List<Asset> flatten(List<ReportSection> sections) {
        // Reserved for future multi-section flattening.
        return new ArrayList<>();
    }
}
