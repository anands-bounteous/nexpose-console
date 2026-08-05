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
        return new AuthResult(false, null, message);
    }

    public boolean isSuccess() { return success; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
}
