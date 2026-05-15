package com.casbytes.core.infrastructure.casbin;

import com.casbytes.core.configuration.properties.CasbinProperties;
import java.nio.charset.StandardCharsets;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.adapter.JDBCAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "casbytes.casbin",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
@ConditionalOnProperty(prefix = "casbytes.casbin", name = "policy-store", havingValue = "jdbc")
public class CasbinJdbcPolicyConfiguration {

  private final CasbinProperties properties;
  private final ResourceLoader resourceLoader;
  private final DataSource dataSource;

  @Bean
  public Enforcer casbinJdbcEnforcer() throws Exception {
    Resource modelResource = resourceLoader.getResource(properties.getModelPath());
    String modelText =
        new String(modelResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    Model model = new Model();
    model.loadModelFromText(modelText);

    JDBCAdapter adapter =
        new JDBCAdapter(
            dataSource, false, properties.getJdbcTableName(), properties.isJdbcAutoCreateTable());
    Enforcer enforcer = new Enforcer(model, adapter);
    if (log.isInfoEnabled()) {
      log.info(
          "Casbin enforcer initialized (store=jdbc, table={}, autoCreateTable={})",
          properties.getJdbcTableName(),
          properties.isJdbcAutoCreateTable());
    }
    return enforcer;
  }
}
