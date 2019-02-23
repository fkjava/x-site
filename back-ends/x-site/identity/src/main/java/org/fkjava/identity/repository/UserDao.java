package org.fkjava.identity.repository;

import org.fkjava.identity.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, String> {

	User findByLoginName(String loginName);

	// 等于查询，where name = ?1
	Page<User> findByName(String keyword, Pageable pageable);

	// 会自动把查询条件前后使用%包起来，使用like查询
	// where name like ?1
	Page<User> findByNameContaining(String keyword, Pageable pageable);
}
