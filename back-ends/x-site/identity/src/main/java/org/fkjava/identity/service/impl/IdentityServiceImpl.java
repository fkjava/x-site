package org.fkjava.identity.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.fkjava.common.data.domain.Result;
import org.fkjava.identity.domain.Role;
import org.fkjava.identity.domain.User;
import org.fkjava.identity.domain.User.Status;
import org.fkjava.identity.repository.RoleRepository;
import org.fkjava.identity.repository.UserDao;
import org.fkjava.identity.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service(value = "identityService")
public class IdentityServiceImpl implements IdentityService {

    private final UserDao userDao;
    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public IdentityServiceImpl(UserDao userDao, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Result save(User user) {

        // 处理角色：固定的始终有、角色不能重复
        // 1.查询所有的固定角色
        List<Role> fiexedRoles = roleRepository.findByFixedTrue();

        // 2.页面传入的角色
        List<Role> roles = user.getRoles();
        if (roles == null) {
            // 不要相信页面的数据！
            // 页面可能传了一个空的集合进来，所以需要在这里判断是否为空、如果为空则自己创建一个
            roles = new LinkedList<>();
            user.setRoles(roles);
        } else {
            // 根据页面传递过来的id，查询所有的角色
            List<String> ids = new LinkedList<>();
//            roles.stream()// 转换为流式API
//                    .map((role) -> {
//                        // role和id的映射关系
//                        // 我们希望：把Role里面的id全部获取出来
//                        return role.getId();
//                    }).forEach(id -> {
//                // 迭代每个id，把id添加到集合里面
//                ids.add(id);
//            });
            roles.stream()
                    // 虽然是引用类的getId方法，但由于map方法实际上是传入Role类型实例
                    // 因此实际上引用的是Role类型的实例方法
                    .map(Role::getId)
                    // 引用List的实例方法
                    .forEach(ids::add);

//			for(Role r : roles) {
//				String id = r.getId();
//				ids.add(id);
//			}

//			roles.stream().map(role -> role.getId()).forEach(ids::add);

            List<Role> tmp = this.roleRepository.findAllById(ids);
            roles.clear();
            // 把页面传过来的角色，全部从数据库里面查询一遍！
            roles.addAll(tmp);
        }

        // 3.确保角色不能重复，需要重写Role的equals方法和hashCode方法，把所有角色添加到Set里面自然就不重复！
        Set<Role> allRoles = new HashSet<>();
        allRoles.addAll(fiexedRoles);
        allRoles.addAll(roles);// 如果后面加入的角色，已经存在Set里面，不会加入！

        // 把整理后的角色放入User里面
        roles.clear();
        roles.addAll(allRoles);

        // 1.检查是否没有id，如果没有id则直接把id设置为null，方便新增
        if (StringUtils.isEmpty(user.getId())) {
            user.setId(null);
        }

        // 每次修改以后，都使用正常状态
        user.setStatus(Status.NORMAL);
        user.setExpiredTime(getExpireTime());

        // 2.根据登录名查询User对象，用于判断登录名是否被占用
        User old = userDao.findByLoginName(user.getLoginName());
        if (old == null) {
            if (!StringUtils.isEmpty(user.getId()) //
                    && StringUtils.isEmpty(user.getPassword())) {
                // 如果是修改（有id），并且密码为空使用旧的密码
                old = userDao.findById(user.getId())
                        .orElseThrow(() ->
                                new RuntimeException("无法根据ID找到 " + user.getLoginName() + " 对应的User对象"));
                user.setPassword(old.getPassword());

            } else {
                // 使用新的密码
                String password = this.passwordEncoder.encode(user.getPassword());
                user.setPassword(password);
            }

            // 没有被占用，直接保存
            userDao.save(user);
        } else {
            // 可能是修改的时候，登录名没有改变
            if (user.getId() != null && user.getId().equals(old.getId())) {
                // 有id表示修改，并且页面传入的id跟数据库查询得到的id相同。
                // 此时表示：页面的登录名和数据库的登录名相同，但是属于同一个用户。
                if (StringUtils.isEmpty(user.getPassword())) {
                    // 密码为空，则直接使用原来的密码
                    user.setPassword(old.getPassword());
                } else {
                    // 使用新的密码
                    String password = this.passwordEncoder.encode(user.getPassword());
                    user.setPassword(password);
                }
                this.userDao.save(user);
            } else {
                // 此时表示：页面的登录名和数据库登录名相同，但是不属于同一个用户，登录名被占用！
                // 如果user的id为null的时候，表示新增，但是数据库有个同名的登录名。
                throw new IllegalArgumentException("登录名重复！");
            }
        }
        return null;
    }

    @Override
    public Page<User> findUsers(String keyword, Integer number) {
        return this.findUsers(keyword, number, 4);
    }

    @Override
    public User findUserById(String id) {
        // orElse : 如果没有从数据库找到对象，那么返回orElse的参数
        return userDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void active(String id) {
        User user = this.userDao.findById(id).orElse(null);
        if (user != null) {
            user.setExpiredTime(getExpireTime());
            user.setStatus(Status.NORMAL);
        }
    }

    private Date getExpireTime() {
        // 加上2个月
        Calendar cal = Calendar.getInstance();

        int month = cal.get(Calendar.MONTH);// 获取月份
        month += 2;
        cal.set(Calendar.MONTH, month);// 把修改的月份设置回去！

        // 2个月之后的时间
        return cal.getTime();
    }

    @Override
    @Transactional
    public void disable(String id) {
        this.userDao.findById(id).ifPresent(user -> user.setStatus(Status.DISABLED));
    }

    @Override
    public Optional<User> findByLoginName(String loginName) {
        User user = this.userDao.findByLoginName(loginName);
        // 把查询到的User转换为Optional
        return Optional.ofNullable(user);
    }

    @Override
    public List<User> findUsers(String keyword) {
        Page<User> page = this.findUsers(keyword, 0, 50);
        return page.getContent();
    }

    private Page<User> findUsers(String keyword, Integer number, Integer size) {
        if (StringUtils.isEmpty(keyword)) {
            keyword = null;
        }

        // 分页条件
        Pageable pageable = PageRequest.of(number, size);

        Page<User> page;
        if (keyword == null) {
            // 分页查询所有数据
            page = userDao.findAll(pageable);
        } else {
            // 根据姓名查询，前后模糊查询
            page = userDao.findByNameContaining(keyword, pageable);
        }
        return page;
    }
}
