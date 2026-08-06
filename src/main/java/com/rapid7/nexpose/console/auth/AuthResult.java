package com.rapid7.nexpose.console.auth;

import com.rapid7.nexpose.console.domain.User;

/** Outcome of an authentication attempt. */
public class AuthResult {

    private final boolean success;
    private final User user;
    private final String message;

    private AuthResult(boolean success, User user, String message) {
        this.success = success;
        this.user = user;
        this.message = message;
    }

    public static AuthResult success(User user) {
        return new AuthResult(true, user, "OK");
    }

    public static AuthResult failure(String message) {
        // BUG (SI-3156): the real failure reason passed in by the caller is
        // discarded in favor of a generic message, so callers/logs downstream
        // can no longer distinguish e.g. "bad password" from "LDAP unreachable".
        return new AuthResult(false, null, "Authentication failed");
    }

    public boolean isSuccess() { return success; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
}
