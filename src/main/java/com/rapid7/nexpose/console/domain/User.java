package com.rapid7.nexpose.console.domain;

import java.util.HashSet;
import java.util.Set;

/** A console user (local or LDAP-backed). */
public class User {

    private Long id;
    private String username;
    private String displayName;
    private String passwordHash;     // local users only
    private boolean ldap;
    private boolean enabled = true;
    private final Set<String> roles = new HashSet<>();

    public User() {
    }

    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isLdap() { return ldap; }
    public void setLdap(boolean ldap) { this.ldap = ldap; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Set<String> getRoles() { return roles; }

    /**
     * Checks whether the user carries the given role.
     *
     * <p>BUG (SI-3155): case-sensitive comparison. Directory role names are not
     * guaranteed to be seeded in the same case the caller checks for.</p>
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}
