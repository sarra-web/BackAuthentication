package com.bezkoder.spring.login.repository;

import com.bezkoder.spring.login.models.ERole;
import com.bezkoder.spring.login.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(ERole name);
}
