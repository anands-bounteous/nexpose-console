package com.rapid7.nexpose.console.auth;

import com.rapid7.nexpose.console.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authenticates local (non-LDAP) users against an in-memory credential store.
 * The built-in administrator is {@code nxadmin} / {@code nxadmin}.
 */
public class LocalAuthenticator implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(LocalAuthenticator.class);

    private final Map<String, User> usersByName = new ConcurrentHashMap<>();

    public LocalAuthenticator() {
        User admin = new User("nxadmin", "Nexpose Administrator");
        admin.setPasswordHash(sha256("nxadmin"));
        admin.getRoles().add("global-admin");
        register(admin);
    }

    public void register(User user) {
        usersByName.put(user.getUsername().toLowerCase(), user);
        log.debug("Registered local user '{}'", user.getUsername());
    }

    @Override
    public boolean supports(String username) {
        User u = usersByName.get(username == null ? "" : username.toLowerCase());
        return u != null && !u.isLdap();
    }

    @Override
    public AuthResult authenticate(String username, String password) {
        User user = usersByName.get(username == null ? "" : username.toLowerCase());
        if (user == null || !user.isEnabled()) {
            log.warn("Local auth failed: unknown or disabled user '{}'", username);
            return AuthResult.failure("Invalid username or password");
        }
        String hash = sha256(password == null ? "" : password);
        if (hash.equals(user.getPasswordHash())) {
            log.info("Local auth succeeded for '{}'", username);
            return AuthResult.success(user);
        }
        log.warn("Local auth failed: bad password for '{}'", username);
        return AuthResult.failure("Invalid username or password");
    }

    private static String sha256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
