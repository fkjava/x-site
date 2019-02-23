package org.fkjava.security.domain;

import java.util.Collection;

import lombok.Getter;
import lombok.Setter;
import org.fkjava.identity.domain.User;
import org.springframework.security.core.GrantedAuthority;

@Getter
@Setter
public class UserDetails extends org.springframework.security.core.userdetails.User {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 用户在数据库里面的id
     */
    private String userId;
    /**
     * 数据库里面的用户的姓名
     */
    private String name;


    /**
     * @param user        数据库里面存储的User对象
     * @param authorities 集合，用户具有的角色、身份。我们在角色的时候有KEY，通常在KEY前面加上ROLE_即可。
     */
    public UserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        super(user.getLoginName(), user.getPassword(), //
                user.getStatus() == User.Status.NORMAL, // 正常
                user.getStatus() != User.Status.EXPIRED, // 不过期
                user.getStatus() != User.Status.EXPIRED, //
                user.getStatus() != User.Status.DISABLED, // 不禁用
                authorities);
        this.userId = user.getId();
        this.name = user.getName();
    }
}
