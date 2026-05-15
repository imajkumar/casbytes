package com.casbytes.core.modules.auth.bootstrap;

/**
 * DDL for {@code users} (kept in sync with Flyway {@code V3}/{@code V4} and {@link
 * com.casbytes.core.modules.auth.domain.UserAccount}).
 */
public final class UsersTableSchema {

  public static final String CREATE_USERS_IF_NOT_EXISTS =
      """
            CREATE TABLE IF NOT EXISTS users (
                id UUID NOT NULL,
                email VARCHAR(255) NOT NULL,
                password_hash VARCHAR(255) NOT NULL,
                role VARCHAR(64) NOT NULL,
                enabled BOOLEAN NOT NULL DEFAULT TRUE,
                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                first_name VARCHAR(128),
                last_name VARCHAR(128),
                display_name VARCHAR(255),
                gender VARCHAR(32),
                phone VARCHAR(64),
                mobile VARCHAR(64),
                job_title VARCHAR(128),
                department VARCHAR(128),
                locale VARCHAR(32),
                time_zone VARCHAR(64),
                CONSTRAINT pk_users PRIMARY KEY (id),
                CONSTRAINT uq_users_email UNIQUE (email)
            )
            """;

  private UsersTableSchema() {}
}
