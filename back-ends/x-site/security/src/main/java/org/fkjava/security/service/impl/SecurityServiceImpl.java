package org.fkjava.security.service.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

import org.fkjava.identity.domain.User;
import org.fkjava.identity.service.IdentityService;
import org.fkjava.security.domain.UserDetails;
import org.fkjava.security.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Spring Security发现内存中有实现了SecurityService的实例，就不会自动创建
@Service
public class SecurityServiceImpl implements SecurityService {

    private final IdentityService identityService;

    @Autowired
    public SecurityServiceImpl(IdentityService identityService) {
        this.identityService = identityService;
    }

    @Override
    public UserDetails loadUserByUsername(String loginName) throws UsernameNotFoundException {
        Optional<User> optional = identityService.findByLoginName(loginName);
        User user = optional.orElseThrow(() -> { //
            return new UsernameNotFoundException("用户的登录名 " + loginName + " 没有对应的用户信息！");//
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
