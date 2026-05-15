package com.casbytes.core.integration.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CasbytesCoreServiceApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  void contextLoads() {
    // Spring context + Flyway + security smoke
  }

  @Test
  void rootPageShowsErpSummaryAndRuntime() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(containsString("CasBytes")))
        .andExpect(content().string(containsString("Uptime")));
  }

  @Test
  void healthEndpointReturnsWrappedResponse() throws Exception {
    mockMvc
        .perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status_code").value(200))
        .andExpect(jsonPath("$.message").value("Success"))
        .andExpect(jsonPath("$.data.status").value("UP"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.meta.correlationId").exists());
  }

  @Test
  void loginWithValidAdminReturnsToken() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"admin@casbytes.com\",\"password\":\"Admin@bytes\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status_code").value(200))
        .andExpect(jsonPath("$.message").value("Login successful"))
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.access_token").isString())
        .andExpect(jsonPath("$.data.token_type").value("Bearer"))
        .andExpect(jsonPath("$.data.expires_in").exists())
        .andExpect(jsonPath("$.data.user.email").value("admin@casbytes.com"))
        .andExpect(jsonPath("$.data.user.role").value("PLATFORM_OWNER"))
        .andExpect(jsonPath("$.data.user.first_name").value("Platform"))
        .andExpect(jsonPath("$.data.user.last_name").value("Administrator"))
        .andExpect(jsonPath("$.data.user.display_name").value("Platform Administrator"))
        .andExpect(jsonPath("$.data.user.gender").value("UNSPECIFIED"));
  }

  @Test
  void loginWithInvalidPasswordReturns401() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"admin@casbytes.com\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status_code").value(401))
        .andExpect(jsonPath("$.message").value("Invalid credentials"))
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void loginWithInvalidPasswordReturns401InFrenchWhenAcceptLanguageFr() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .header("Accept-Language", "fr")
                .contentType(APPLICATION_JSON)
                .content("{\"email\":\"admin@casbytes.com\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status_code").value(401))
        .andExpect(jsonPath("$.message").value("Identifiants invalides"))
        .andExpect(jsonPath("$.error.message").value("Identifiants invalides"))
        .andExpect(jsonPath("$.success").value(false));
  }
}
