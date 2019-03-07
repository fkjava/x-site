package org.fkjava.security.sms;

import org.fkjava.identity.domain.User;
import org.fkjava.identity.service.IdentityService;
import org.fkjava.security.domain.UserDetails;
import org.fkjava.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

@Service(value = "smsSecurityService")
public class SMSSecurityService implements SecurityService {

    private final IdentityService identityService;

    @Autowired
    public SMSSecurityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        Optional<User> optional = identityService.findByPhone(phone);
        User user = optional.orElseThrow(() -> { //
            return new UsernameNotFoundException("手机号码 " + phone + " 没有对应的用户信息！");//
        });
        Collection<GrantedAuthority> authorities = new HashSet<>();
        // 获取所有的角色，在角色的KEY前面加上ROLE_开头作为【已授权的身份】
        // ROLE_是Spring Security要求的
        user.getRoles().forEach(role -> {
            GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_" + role.getRoleKey());
            authorities.add(ga);
        });

        return new UserDetails(user, authorities);
    }
}
