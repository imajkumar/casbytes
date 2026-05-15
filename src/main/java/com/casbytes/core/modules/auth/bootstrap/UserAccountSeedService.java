package com.casbytes.core.modules.auth.bootstrap;

import com.casbytes.core.modules.auth.domain.UserAccount;
import com.casbytes.core.modules.auth.domain.UserAccountRepository;
import com.casbytes.core.modules.auth.domain.UserRole;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountSeedService {

  static final UUID ROOT_ADMIN_ID = UUID.fromString("a0000000-0000-4000-8000-000000000001");
  static final String ROOT_ADMIN_EMAIL = "admin@casbytes.com";
  static final String ROOT_ADMIN_PASSWORD_ENCODED =
      "{bcrypt}$2b$10$DwaqVslAr56fJ77gTjFshO6U5mfPeSdczmM9eSz5waV07eSxtVdyW";

  private final UserAccountRepository userAccountRepository;

  /**
   * Idempotent: ensures root admin exists for {@value #ROOT_ADMIN_EMAIL} (same row as Flyway
   * {@code V3__users_and_root_admin.sql}). Used at startup when Flyway is off or DB was wiped.
   */
  @Transactional
  public void ensureRootAdminSeeded() {
    if (userAccountRepository.existsByEmailIgnoreCase(ROOT_ADMIN_EMAIL)) {
      return;
    }
    Instant now = Instant.now();
    UserAccount admin = new UserAccount();
    admin.setId(ROOT_ADMIN_ID);
    admin.setEmail(ROOT_ADMIN_EMAIL);
    admin.setPasswordHash(ROOT_ADMIN_PASSWORD_ENCODED);
    admin.setRole(UserRole.PLATFORM_OWNER);
    admin.setEnabled(true);
    admin.setCreatedAt(now);
    admin.setUpdatedAt(now);
    admin.setFirstName("Platform");
    admin.setLastName("Administrator");
    admin.setDisplayName("Platform Administrator");
    admin.setGender("UNSPECIFIED");
    admin.setLocale("en");
    admin.setTimeZone("UTC");
    userAccountRepository.save(admin);
  }
}
