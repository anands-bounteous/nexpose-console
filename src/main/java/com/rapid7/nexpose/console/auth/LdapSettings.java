package com.rapid7.nexpose.console.auth;

/**
 * LDAP connection settings, normally bound from application.properties
 * (nexpose.ldap.*). See config defect NEX-3108: the shipped defaults point at a
 * host/base-DN that does not exist, so LDAP binds fail.
 */
public class LdapSettings {

    private boolean enabled;
    private String url;             // e.g. ldap://ldap.lab.rapid7.com:389
    private String baseDn;          // e.g. dc=rapid7,dc=com
    private String userDnPattern;   // e.g. uid={0},ou=people
    private int connectTimeoutMs = 5000;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getBaseDn() { return baseDn; }
    public void setBaseDn(String baseDn) { this.baseDn = baseDn; }

    public String getUserDnPattern() { return userDnPattern; }
    public void setUserDnPattern(String userDnPattern) { this.userDnPattern = userDnPattern; }

    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }

    public String fullUserDn(String username) {
        return userDnPattern.replace("{0}", username) + "," + baseDn;
    }
}
