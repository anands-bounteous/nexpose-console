package com.rapid7.nexpose.console.exception;

/** Raised when the LDAP directory cannot be reached or bound. */
public class LdapConnectionException extends NexposeException {

    public LdapConnectionException(String message) {
        super("NEXL-AUTH-002", message);
    }

    public LdapConnectionException(String message, Throwable cause) {
        super("NEXL-AUTH-002", message, cause);
    }
}
