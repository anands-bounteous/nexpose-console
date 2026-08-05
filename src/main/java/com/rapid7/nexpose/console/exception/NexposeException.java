package com.rapid7.nexpose.console.exception;

/**
 * Base type for all Nexpose console exceptions. Carries a stable error code so
 * the UI and log analysis can key on it.
 */
public class NexposeException extends RuntimeException {

    private final String errorCode;

    public NexposeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public NexposeException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
