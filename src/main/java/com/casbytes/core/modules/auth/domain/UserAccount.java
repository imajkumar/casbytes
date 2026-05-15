package com.casbytes.core.modules.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserAccount {

  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(nullable = false, unique = true, length = 255)
  private String email;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 64)
  private UserRole role = UserRole.USER;

  @Column(nullable = false)
  private boolean enabled = true;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "first_name", length = 128)
  private String firstName;

  @Column(name = "last_name", length = 128)
  private String lastName;

  @Column(name = "display_name", length = 255)
  private String displayName;

  @Column(length = 32)
  private String gender;

  @Column(length = 64)
  private String phone;

  @Column(length = 64)
  private String mobile;

  @Column(name = "job_title", length = 128)
  private String jobTitle;

  @Column(length = 128)
  private String department;

  @Column(length = 32)
  private String locale;

  @Column(name = "time_zone", length = 64)
  private String timeZone;
}
