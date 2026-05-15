package com.casbytes.core.modules.auth.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

  Optional<UserAccount> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);
}
