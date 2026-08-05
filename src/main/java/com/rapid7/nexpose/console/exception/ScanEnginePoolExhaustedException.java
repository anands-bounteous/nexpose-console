package com.rapid7.nexpose.console.exception;

/** Raised when no scan engine slot can be acquired from the pool. */
public class ScanEnginePoolExhaustedException extends NexposeException {

    public ScanEnginePoolExhaustedException(String message) {
        super("NEXL-SCAN-002", message);
    }

    public ScanEnginePoolExhaustedException(String message, Throwable cause) {
        super("NEXL-SCAN-002", message, cause);
    }
}
