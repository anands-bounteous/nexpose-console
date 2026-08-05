package com.rapid7.nexpose.console.exception;

/** Raised when asset correlation fails. */
public class CorrelationException extends NexposeException {

    public CorrelationException(String message) {
        super("NEXL-CORR-001", message);
    }

    public CorrelationException(String message, Throwable cause) {
        super("NEXL-CORR-001", message, cause);
    }
}
