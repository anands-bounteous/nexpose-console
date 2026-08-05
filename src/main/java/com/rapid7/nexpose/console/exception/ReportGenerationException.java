package com.rapid7.nexpose.console.exception;

/** Raised when a report cannot be generated or serialised. */
public class ReportGenerationException extends NexposeException {

    public ReportGenerationException(String message) {
        super("NEXL-RPT-001", message);
    }

    public ReportGenerationException(String message, Throwable cause) {
        super("NEXL-RPT-001", message, cause);
    }
}
