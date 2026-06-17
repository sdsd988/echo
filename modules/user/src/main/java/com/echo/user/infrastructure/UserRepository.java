package com.echo.user.infrastructure;

import com.echo.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User JPA 리포지토리. infrastructure 계층.
 */
public interface UserRepository extends JpaRepository<User, Long> {
}
