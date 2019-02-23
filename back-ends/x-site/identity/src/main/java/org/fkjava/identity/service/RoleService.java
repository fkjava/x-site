package org.fkjava.identity.service;

import java.util.List;

import org.fkjava.common.data.domain.Result;
import org.fkjava.identity.domain.Role;

public interface RoleService {

	List<Role> findAllRoles();

	Result save(Role role);

	Result deleteById(String id);

	List<Role> findAllNotFixed();

	List<Role> findAll();

}
