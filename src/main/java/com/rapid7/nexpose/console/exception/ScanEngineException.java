package com.rapid7.nexpose.console.exception;

/** Raised when the scan engine fails to execute or communicate. */
public class ScanEngineException extends NexposeException {

    public ScanEngineException(String message) {
        super("NEXL-SCAN-001", message);
    }

    public ScanEngineException(String message, Throwable cause) {
        super("NEXL-SCAN-001", message, cause);
    }
}
