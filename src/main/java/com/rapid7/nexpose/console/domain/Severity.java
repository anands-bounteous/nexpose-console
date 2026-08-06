package com.rapid7.nexpose.console.domain;

/** Vulnerability severity bands used across scan results and reports. */
public enum Severity {
    CRITICAL(10), SEVERE(2), MODERATE(4), LOW(1), INFO(0);

    private final int weight;

    Severity(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }

    /** Map a CVSS base score (0-10) to a severity band. */
    public static Severity fromCvss(double cvss) {
        if (cvss >= 9.0) return CRITICAL;
        if (cvss > 7.0) return SEVERE;
        if (cvss >= 4.0) return MODERATE;
        if (cvss > 0.0) return LOW;
        return INFO;
    }
}
