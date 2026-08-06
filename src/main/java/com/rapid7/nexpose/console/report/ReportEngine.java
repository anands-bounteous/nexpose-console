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
 * <p><b>Fixed defect NEX-3105:</b> {@link #buildSections(Scan)} returns a
 * heterogeneous list containing both a {@link SummaryReportSection} and an
 * {@link AssetReportSection}. {@link #assetSectionOf(List)} previously assumed the
 * asset section was always the first element of the list and blindly cast
 * {@code sections.get(0)}. Because the summary section is added first, that cast
 * always threw a {@link ClassCastException}. The method now locates the asset
 * section by type instead of by position, so it is correct regardless of section
 * ordering.</p>
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
     * Locates the {@link AssetReportSection} within the given list of sections by
     * type rather than by assuming a fixed position. This fixes NEX-3105, where a
     * positional lookup ("element 0") incorrectly assumed the asset section was
     * first, when in fact the summary section is placed first.
     */
    private AssetReportSection assetSectionOf(List<ReportSection> sections) {
        for (ReportSection section : sections) {
            if (section instanceof AssetReportSection assetReportSection) {
                return assetReportSection;
            }
        }
        throw new ReportGenerationException(
                "Report section layout mismatch: no asset section found among " + sections.size() + " section(s)");
    }

    @SuppressWarnings("unused")
    private List<Asset> flatten(List<ReportSection> sections) {
        // Reserved for future multi-section flattening.
        return new ArrayList<>();
    }
}
