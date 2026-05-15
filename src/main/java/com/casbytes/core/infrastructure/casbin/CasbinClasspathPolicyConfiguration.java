package com.casbytes.core.infrastructure.casbin;

import com.casbytes.core.configuration.properties.CasbinProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.file_adapter.FileAdapter;
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
@ConditionalOnProperty(
    prefix = "casbytes.casbin",
    name = "policy-store",
    havingValue = "classpath",
    matchIfMissing = true)
public class CasbinClasspathPolicyConfiguration {

  private final CasbinProperties properties;
  private final ResourceLoader resourceLoader;

  @Bean
  public Enforcer casbinClasspathEnforcer() throws IOException {
    Resource modelResource = resourceLoader.getResource(properties.getModelPath());
    Resource policyResource = resourceLoader.getResource(properties.getPolicyPath());

    String modelText =
        new String(modelResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    String policyText =
        new String(policyResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

    Model model = new Model();
    model.loadModelFromText(modelText);
    FileAdapter adapter =
        new FileAdapter(new ByteArrayInputStream(policyText.getBytes(StandardCharsets.UTF_8)));
    Enforcer enforcer = new Enforcer(model, adapter);
    if (log.isInfoEnabled()) {
      log.info(
          "Casbin enforcer initialized (store=classpath, model={}, policy={})",
          properties.getModelPath(),
          properties.getPolicyPath());
    }
    return enforcer;
  }
}
