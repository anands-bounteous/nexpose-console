package com.rapid7.nexpose.console.exception;

/** Raised when a risk score cannot be computed. */
public class RiskCalculationException extends NexposeException {

    public RiskCalculationException(String message) {
        super("NEXL-RISK-001", message);
    }

    public RiskCalculationException(String message, Throwable cause) {
        super("NEXL-RISK-001", message, cause);
    }
}
