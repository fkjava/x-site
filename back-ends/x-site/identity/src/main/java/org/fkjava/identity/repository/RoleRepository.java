package org.fkjava.identity.repository;

import java.util.List;
import java.util.Optional;

import org.fkjava.identity.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

	Optional<Role> findByRoleKey(String roleKey);

	List<Role> findByFixedTrue();

	List<Role> findByFixedFalseOrderByName();

}
