package com.rapid7.nexpose.console.exception;

/** Raised when authentication fails. */
public class AuthenticationException extends NexposeException {

    public AuthenticationException(String message) {
        super("NEXL-AUTH-001", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("NEXL-AUTH-001", message, cause);
    }
}
