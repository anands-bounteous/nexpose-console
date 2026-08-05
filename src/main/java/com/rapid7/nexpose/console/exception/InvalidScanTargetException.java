package com.rapid7.nexpose.console.exception;

/** Raised when a scan target (IP / CIDR / hostname) cannot be parsed. */
public class InvalidScanTargetException extends NexposeException {

    public InvalidScanTargetException(String message) {
        super("NEXL-SCAN-003", message);
    }

    public InvalidScanTargetException(String message, Throwable cause) {
        super("NEXL-SCAN-003", message, cause);
    }
}
