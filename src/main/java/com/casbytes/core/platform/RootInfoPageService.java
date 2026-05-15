package com.casbytes.core.platform;

import com.casbytes.core.configuration.properties.CasbytesInfoProperties;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RootInfoPageService {

  private static final DateTimeFormatter ISO_UTC =
      DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

  private final ObjectProvider<BuildProperties> buildProperties;
  private final CasbytesInfoProperties infoProperties;

  public String renderHtml() {
    Instant jvmStart = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());
    Instant now = Instant.now();
    Duration uptime = Duration.between(jvmStart, now);

    Optional<BuildProperties> build = Optional.ofNullable(buildProperties.getIfAvailable());
    String version =
        build.map(BuildProperties::getVersion).orElse(infoProperties.getVersionFallback());
    String buildTime =
        build
            .map(BuildProperties::getTime)
            .map(t -> ISO_UTC.format(t.atOffset(ZoneOffset.UTC)))
            .orElse(
                "— (run <code>mvn package</code> or <code>mvn compile</code> to generate build-info)");

    String product = escapeHtml(infoProperties.getProductName());
    String tagline = escapeHtml(infoProperties.getTagline());

    return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>%s</title>
                  <style>
                    :root { color-scheme: light dark; }
                    body { font-family: system-ui, Segoe UI, Roboto, sans-serif; margin: 2rem; line-height: 1.5; max-width: 52rem; }
                    h1 { font-size: 1.5rem; margin-bottom: 0.25rem; }
                    .muted { color: #666; }
                    .panel { border: 1px solid #ccc; border-radius: 8px; padding: 1rem 1.25rem; margin: 1.25rem 0; background: rgba(127,127,127,.06); }
                    dl { display: grid; grid-template-columns: 10rem 1fr; gap: 0.35rem 1rem; margin: 0; }
                    dt { font-weight: 600; }
                    dd { margin: 0; }
                    ul { margin: 0.5rem 0 0; padding-left: 1.2rem; }
                    a { color: #0b57d0; }
                    code { font-size: 0.9em; }
                  </style>
                </head>
                <body>
                  <h1>%s</h1>
                  <p class="muted">%s</p>

                  <div class="panel">
                    <strong>Release &amp; runtime</strong>
                    <dl>
                      <dt>Artifact version</dt><dd><code>%s</code></dd>
                      <dt>Build time (UTC)</dt><dd>%s</dd>
                      <dt>JVM started (UTC)</dt><dd>%s</dd>
                      <dt>Uptime (this process)</dt><dd>%s</dd>
                    </dl>
                  </div>

                  <div class="panel">
                    <strong>What this service provides</strong>
                    <ul>
                      <li>Versioned REST API under <code>/api/v1</code> (OpenAPI / Swagger)</li>
                      <li>PostgreSQL persistence with Flyway migrations</li>
                      <li>Security: OAuth2 JWT resource server and optional local JWT login</li>
                      <li>Casbin-backed authorization (classpath or JDBC policies)</li>
                      <li>Redis, Kafka, and Elasticsearch integration for platform features</li>
                      <li>Correlation IDs, Actuator health/metrics, structured logging</li>
                    </ul>
                  </div>

                  <p>
                    <a href="/swagger-ui.html">OpenAPI (Swagger UI)</a>
                    · <a href="/api/v1/health">Aggregated health</a>
                    · <a href="/actuator/info">Actuator info</a> <span class="muted">(auth required)</span>
                  </p>
                </body>
                </html>
                """
        .formatted(
            product,
            product,
            tagline,
            escapeHtml(version),
            buildTime,
            ISO_UTC.format(jvmStart.atOffset(ZoneOffset.UTC)),
            humanUptime(uptime));
  }

  private static String humanUptime(Duration d) {
    long s = d.getSeconds();
    if (s < 60) {
      return s + "s";
    }
    long m = s / 60;
    if (m < 60) {
      return m + "m " + (s % 60) + "s";
    }
    long h = m / 60;
    if (h < 48) {
      return h + "h " + (m % 60) + "m";
    }
    long days = h / 24;
    return days + "d " + (h % 24) + "h";
  }

  private static String escapeHtml(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
