package com.rapid7.nexpose.console.auth;

/** Strategy for authenticating a username/password pair. */
public interface Authenticator {

    /** @return true if this authenticator is responsible for the given username. */
    boolean supports(String username);

    AuthResult authenticate(String username, String password);
}
