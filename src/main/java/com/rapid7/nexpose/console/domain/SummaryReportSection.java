package com.rapid7.nexpose.console.domain;

/** A report section holding scan-level summary counters. */
public class SummaryReportSection extends ReportSection {

    private final int assetCount;
    private final int vulnerabilityCount;
    private final double averageRisk;

    public SummaryReportSection(int assetCount, int vulnerabilityCount, double averageRisk) {
        super("Summary");
        this.assetCount = assetCount;
        this.vulnerabilityCount = vulnerabilityCount;
        this.averageRisk = averageRisk;
    }

    public int getAssetCount() { return assetCount; }
    public int getVulnerabilityCount() { return vulnerabilityCount; }
    public double getAverageRisk() { return averageRisk; }

    @Override
    public String elementName() {
        return "summary";
    }
}
