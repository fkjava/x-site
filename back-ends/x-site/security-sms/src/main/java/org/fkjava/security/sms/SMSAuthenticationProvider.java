package org.fkjava.security.sms;

import org.fkjava.sms.service.ShortMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class SMSAuthenticationProvider implements AuthenticationProvider {

    private UserDetailsService userDetailsService;
    private ShortMessageService shortMessageService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SMSAuthenticationToken smsAuthenticationToken = (SMSAuthenticationToken) (authentication);
        UserDetails user = userDetailsService.loadUserByUsername((String) smsAuthenticationToken.getPrincipal());
        if (null == user) {
            throw new UsernameNotFoundException("无法获取用户信息");
        }

        String userCode = smsAuthenticationToken.getVerify();
        if (shortMessageService.verify(
                smsAuthenticationToken.getSessionId(),
                smsAuthenticationToken.getPrincipal().toString(),
                userCode)) {

            SMSAuthenticationToken tokenResult = new SMSAuthenticationToken(
                    smsAuthenticationToken.getSessionId(),
                    smsAuthenticationToken.getPrincipal(),
                    user.getAuthorities());
            tokenResult.setDetails(smsAuthenticationToken.getDetails());
            return tokenResult;
        } else {
            throw new BadCredentialsException("短信登录出现问题：验证码错误或者失效！");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SMSAuthenticationToken.class.isAssignableFrom(authentication);
    }


    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setShortMessageService(ShortMessageService shortMessageService) {
        this.shortMessageService = shortMessageService;
    }
}
