package com.rapid7.nexpose.console.domain;

/** Lifecycle states of a scan. */
public enum ScanStatus {
    PENDING, RUNNING, INTEGRATING, COMPLETED, FAILED, STOPPED, ABORTED
}
