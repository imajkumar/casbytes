package com.casbytes.core.infrastructure.casbin;

import lombok.RequiredArgsConstructor;
import org.casbin.jcasbin.main.Enforcer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(Enforcer.class)
public class CasbinAuthorizationService {

    private final Enforcer enforcer;

    public boolean enforce(String subject, String object, String action) {
        return enforcer.enforce(subject, object, action);
    }
}
