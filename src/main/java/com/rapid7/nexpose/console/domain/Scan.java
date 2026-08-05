package com.rapid7.nexpose.console.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** A scan execution and its results. */
public class Scan {

    private Long id;
    private String name;
    private String engineName;
    private ScanStatus status = ScanStatus.PENDING;
    private Instant startedAt;
    private Instant finishedAt;
    private final List<String> targets = new ArrayList<>();
    private final List<Asset> assets = new ArrayList<>();
    private String failureReason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEngineName() { return engineName; }
    public void setEngineName(String engineName) { this.engineName = engineName; }

    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public List<String> getTargets() { return targets; }
    public List<Asset> getAssets() { return assets; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public int totalVulnerabilities() {
        int total = 0;
        for (Asset asset : assets) {
            if (asset.getVulnerabilities() != null) {
                total += asset.getVulnerabilities().size();
            }
        }
        return total;
    }
}
