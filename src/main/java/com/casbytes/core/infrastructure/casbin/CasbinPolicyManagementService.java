package com.casbytes.core.infrastructure.casbin;

import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(Enforcer.class)
public class CasbinPolicyManagementService {

  private final Enforcer enforcer;

  /**
   * Reloads policies from the active {@link org.casbin.jcasbin.persist.Adapter} (classpath file or JDBC).
   */
  public void reloadPolicies() {
    enforcer.clearPolicy();
    enforcer.loadPolicy();
  }
}
