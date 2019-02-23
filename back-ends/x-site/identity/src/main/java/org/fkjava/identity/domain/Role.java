package org.fkjava.identity.domain;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "id_role")
@Cacheable // 可以被缓存的，提高查询效率
@Getter
@Setter
public class Role implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(length = 36)
    private String id;
    @Column(length = 20)
    private String name;
    // 不能使用key作为属性的名称，因为key是数据库的关键字
    @Column(unique = true)
    private String roleKey;
    /**
     * 是否固定的角色
     */
    private boolean fixed = false;


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((roleKey == null) ? 0 : roleKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Role other = (Role) obj;
        if (roleKey == null) {
            return other.roleKey == null;
        } else return roleKey.equals(other.roleKey);
    }
}
