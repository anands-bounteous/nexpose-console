package com.rapid7.nexpose.console.auth;

import com.rapid7.nexpose.console.exception.LdapConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

/**
 * Authenticates directory (LDAP) users by binding to the configured LDAP server
 * with the supplied credentials, using JDK JNDI.
 *
 * <p><b>Known defect NEX-3108 (configuration issue):</b> the LDAP settings shipped
 * in {@code application.properties} point at a directory host that does not exist
 * in the lab ({@code ldap://ldap.internal.rapid7.local:389}) and a base DN that
 * does not match the tree ({@code dc=rapid7,dc=local} instead of
 * {@code dc=rapid7,dc=com}). Every LDAP bind therefore fails with a JNDI
 * {@code CommunicationException}, wrapped here as {@link LdapConnectionException}.
 * The class code is correct; the <em>configuration</em> is wrong. Fixing the two
 * properties resolves it — no code change required.</p>
 */
public class LdapAuthenticator implements Authenticator {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthenticator.class);

    private final LdapSettings settings;

    public LdapAuthenticator(LdapSettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean supports(String username) {
        // Any non-local username is treated as a directory user when LDAP is on.
        return settings.isEnabled();
    }

    @Override
    public AuthResult authenticate(String username, String password) {
        if (!settings.isEnabled()) {
            return AuthResult.failure("LDAP authentication is disabled");
        }
        String userDn = settings.fullUserDn(username);
        log.info("Attempting LDAP bind for '{}' at {} (dn={})",
                username, settings.getUrl(), userDn);

        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, settings.getUrl());               // <-- misconfigured (NEX-3108)
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password == null ? "" : password);
        env.put("com.sun.jndi.ldap.connect.timeout",
                String.valueOf(settings.getConnectTimeoutMs()));

        InitialDirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);                           // throws CommunicationException
            log.info("LDAP bind succeeded for '{}'", username);
            return AuthResult.success(directoryUser(username));
        } catch (NamingException e) {
            // Misconfigured host/base DN -> CommunicationException lands here.
            log.error("LDAP bind failed for '{}' against {}: {}",
                    username, settings.getUrl(), e.toString());
            throw new LdapConnectionException(
                    "Unable to authenticate '" + username + "' against LDAP server "
                            + settings.getUrl(), e);
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ignored) {
                    // best-effort close
                }
            }
        }
    }

    private com.rapid7.nexpose.console.domain.User directoryUser(String username) {
        com.rapid7.nexpose.console.domain.User u =
                new com.rapid7.nexpose.console.domain.User(username, username);
        u.setLdap(true);
        u.getRoles().add("user");
        return u;
    }
}
