package org.fkjava.identity.service.impl;

import java.util.List;

import org.fkjava.common.data.domain.Result;
import org.fkjava.identity.domain.Role;
import org.fkjava.identity.repository.RoleRepository;
import org.fkjava.identity.service.RoleService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RoleServiceImpl implements RoleService, InitializingBean {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
//        // 在服务启动的时候，检查是否有预置的角色，如果没有则自动加上
//        Role admin = this.roleRepository.findByRoleKey("ADMIN")// 需要手动加入给用户的
//                .orElse(new Role());
//        admin.setName("超级管理员");
//        admin.setRoleKey("ADMIN");
//        this.save(admin);
//
//        // 所有用户的默认角色
//        // 通过findByRoleKey查询对象
//        Role user = this.roleRepository.findByRoleKey("USER")//
//                // 如果没有查询到，则new一个
//                .orElse(new Role());
//        user.setName("普通用户");
//        user.setRoleKey("USER");
//        user.setFixed(true);// 固定的、每个用户都具有的
//        this.save(user);


        List<Role> roles = this.roleRepository.findAll();
        roles.stream()
                .filter(role -> role.getRoleKey().equals("ADMIN"))
                .findFirst()
                .ifPresentOrElse(
                        role -> {
                        },
                        () -> {
                            Role admin = new Role();
                            admin.setName("超级管理员");
                            admin.setRoleKey("ADMIN");
                            admin.setFixed(false);
                            roleRepository.save(admin);
                        }
                );

        roles.stream()
                .filter(role -> role.getRoleKey().equals("USER"))
                .findFirst()
                .ifPresentOrElse(
                        role -> {
                            // 有普通用户的角色时，什么都不处理
                        },
                        () -> {
                            // 没有普通用户的角色，则自动增加一个
                            Role user = new Role();
                            user.setName("普通用户");
                            user.setRoleKey("USER");
                            user.setFixed(true);
                            roleRepository.save(user);
                        }
                );
    }

    @Override
    public List<Role> findAllRoles() {
        return this.roleRepository.findAll();
    }

    @Override
    public Result save(Role role) {
        if (StringUtils.isEmpty(role.getId())) {
            role.setId(null);
        }

        Role old = roleRepository.findByRoleKey(role.getRoleKey())
                // 如果没有找到，返回null
                .orElse(null);
        if (
            // id没有表示新增，old是空的，表示数据库没有重复的角色
                (role.getId() == null && old == null)
                        // 修改的时候，根据key从数据库找到一个Role，但是id相同，此时只是修改了名称、没有修改key
                        || (role.getId() != null && old != null && role.getId().equals(old.getId()))//
                        // 修改，但是key从数据库里面没有找到记录，表示key也修改了！
                        || (role.getId() != null && old == null)) {
            roleRepository.save(role);
        } else {
            // 要么id为空、数据库不为空
            // 要么id不为空、数据库也不为空，但是id不相同
            throw new IllegalArgumentException("角色的KEY是唯一的，不能重复！");
        }
        return null;
    }

    @Override
    public Result deleteById(String id) {
        roleRepository.deleteById(id);
        return null;
    }

    @Override
    public List<Role> findAllNotFixed() {
        return this.roleRepository.findByFixedFalseOrderByName();
    }

    @Override
    public List<Role> findAll() {
        return this.roleRepository.findAll();
    }
}
