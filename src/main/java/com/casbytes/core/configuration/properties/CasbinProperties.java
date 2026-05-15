package com.casbytes.core.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "casbytes.casbin")
public class CasbinProperties {

    private boolean enabled = true;

    /**
     * Where policy lines are loaded from: {@code classpath} (CSV) or {@code jdbc} (same database as the app).
     */
    private PolicyStore policyStore = PolicyStore.CLASSPATH;

    /**
     * When true, exposes {@code POST /api/v1/admin/casbin/reload} to reload policies from the active store.
     * Keep disabled in production unless strictly gated (OAuth scopes + network policy).
     */
    private boolean reloadEndpointEnabled = false;

    /**
     * JDBC adapter table name (must match Flyway / DBA DDL when not using adapter auto-create).
     */
    private String jdbcTableName = "casbin_rule";

    /**
     * When true, {@link org.casbin.adapter.JDBCAdapter} may create/alter the policy table on startup.
     * Prefer false when Flyway owns the schema.
     */
    private boolean jdbcAutoCreateTable = false;

    /**
     * Classpath location for the Casbin model file.
     */
    private String modelPath = "classpath:casbin/model.conf";

    /**
     * Classpath location for the default policy CSV (used only when policy-store=classpath).
     */
    private String policyPath = "classpath:casbin/policy.csv";

    public enum PolicyStore {
        CLASSPATH,
        JDBC
    }
}
