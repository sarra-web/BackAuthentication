package com.bezkoder.spring.login.repository;

import com.bezkoder.spring.login.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  Boolean existsByUsername(String username);

  Boolean existsByEmail(String email);

  Page<User> findAll(Pageable pageable);
  User findOneById(String id);

  Page<User> findByUsernameContaining(String name, Pageable pageable);

}
