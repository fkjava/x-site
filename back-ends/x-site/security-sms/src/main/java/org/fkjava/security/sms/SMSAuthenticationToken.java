package org.fkjava.security.sms;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class SMSAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 420L;
    private Object principal;
    private String verify;
    private String sessionId;

    public SMSAuthenticationToken(String sessionId, String phone, String verify) {
        super(null);
        this.sessionId = sessionId;
        this.principal = phone;
        this.verify = verify;
        this.setAuthenticated(false);
    }

    public SMSAuthenticationToken(String sessionId,Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.sessionId = sessionId;
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    public Object getPrincipal() {
        return this.principal;
    }

    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        } else {
            super.setAuthenticated(false);
        }
    }

    public String getVerify() {
        return verify;
    }

    public void setVerify(String verify) {
        this.verify = verify;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
