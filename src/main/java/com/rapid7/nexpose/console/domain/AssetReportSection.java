package com.rapid7.nexpose.console.domain;

import java.util.List;

/** A report section listing assets and their vulnerabilities. */
public class AssetReportSection extends ReportSection {

    private final List<Asset> assets;

    public AssetReportSection(List<Asset> assets) {
        super("Assets");
        this.assets = assets;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    @Override
    public String elementName() {
        return "assets";
    }
}
