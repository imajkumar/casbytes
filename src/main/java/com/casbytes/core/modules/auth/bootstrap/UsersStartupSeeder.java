package com.casbytes.core.modules.auth.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures {@code users} exists and the root admin row is present. Complements Flyway when enabled;
 * when Flyway is off, still creates the table and seed so API login works.
 */
@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class UsersStartupSeeder implements ApplicationRunner {

  private final JdbcTemplate jdbcTemplate;
  private final UserAccountSeedService userAccountSeedService;

  @Override
  public void run(ApplicationArguments args) {
    jdbcTemplate.execute(UsersTableSchema.CREATE_USERS_IF_NOT_EXISTS);
    userAccountSeedService.ensureRootAdminSeeded();
    log.info("Users startup seeder finished (users table if needed, root admin row if missing)");
  }
}
