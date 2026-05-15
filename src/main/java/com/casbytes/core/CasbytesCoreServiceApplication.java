package com.casbytes.core;

import com.casbytes.core.configuration.properties.AuthProperties;
import com.casbytes.core.configuration.properties.CasbinProperties;
import com.casbytes.core.configuration.properties.CasbytesInfoProperties;
import com.casbytes.core.configuration.properties.CasbytesProperties;
import com.casbytes.core.configuration.properties.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.DataRedisRepositoriesAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(
    exclude = {
      UserDetailsServiceAutoConfiguration.class,
      DataRedisRepositoriesAutoConfiguration.class
    })
@EntityScan(basePackages = "com.casbytes.core")
@EnableAsync
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.casbytes.core")
@EnableConfigurationProperties({
  AuthProperties.class,
  CasbinProperties.class,
  CasbytesInfoProperties.class,
  CasbytesProperties.class,
  JwtProperties.class
})
@SuppressWarnings("PMD.UseUtilityClass") // Spring Boot entry point
public class CasbytesCoreServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(CasbytesCoreServiceApplication.class, args);
  }
}
