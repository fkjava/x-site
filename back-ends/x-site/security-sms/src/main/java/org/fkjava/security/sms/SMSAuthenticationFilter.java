package org.fkjava.security.sms;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SMSAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private String mobileParameter = "phone";
    private String verifyParameter = "userCode";
    private boolean postOnly = true;

    public SMSAuthenticationFilter(String loginUrl) {
        super(loginUrl);
    }

    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        } else {
            String phone = this.obtainMobile(request);
            if (phone == null) {
                phone = "";
            }
            phone = phone.trim();

            String verify = this.obtainVerify(request);
            if (verify == null) {
                verify = "";
            }

            verify = verify.trim();
            String sessionId = request.getSession().getId();
            SMSAuthenticationToken authRequest = new SMSAuthenticationToken(sessionId, phone, verify);
            this.setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);
        }
    }

    private String obtainVerify(HttpServletRequest request) {
        return request.getParameter(this.verifyParameter);
    }


    protected String obtainMobile(HttpServletRequest request) {
        return request.getParameter(this.mobileParameter);
    }

    protected void setDetails(HttpServletRequest request, SMSAuthenticationToken authRequest) {
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
    }
}
