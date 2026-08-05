package com.rapid7.nexpose.console.domain;

import java.util.List;
import java.util.Objects;

/**
 * A scanned host/asset.
 *
 * NOTE: {@link #vulnerabilities} may be {@code null} when an asset has been
 * discovered but not yet fingerprinted (a live-host-only result). Callers must
 * treat the list as possibly-null. (See defect NEX-3101.)
 */
public class Asset {

    private Long id;
    private String ipAddress;
    private String hostName;
    private String operatingSystem;
    private String macAddress;
    private boolean live;
    private List<Vulnerability> vulnerabilities;   // may be null (see class doc)
    private double riskScore;

    public Asset() {
    }

    public Asset(String ipAddress, String hostName, String operatingSystem) {
        this.ipAddress = ipAddress;
        this.hostName = hostName;
        this.operatingSystem = operatingSystem;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getOperatingSystem() { return operatingSystem; }
    public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public boolean isLive() { return live; }
    public void setLive(boolean live) { this.live = live; }

    public List<Vulnerability> getVulnerabilities() { return vulnerabilities; }
    public void setVulnerabilities(List<Vulnerability> vulnerabilities) { this.vulnerabilities = vulnerabilities; }

    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Asset asset)) return false;
        return Objects.equals(ipAddress, asset.ipAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ipAddress);
    }

    @Override
    public String toString() {
        return "Asset{ip=" + ipAddress + ", host=" + hostName + ", os=" + operatingSystem + "}";
    }
}
