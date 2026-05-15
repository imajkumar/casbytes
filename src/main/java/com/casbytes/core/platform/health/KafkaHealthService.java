package com.casbytes.core.platform.health;

import com.casbytes.core.configuration.properties.CasbytesProperties;
import com.casbytes.core.platform.health.dto.HealthStatusDto;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaHealthService {

  private final CasbytesProperties casbytesProperties;

  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  public HealthStatusDto check() {
    if (!casbytesProperties.getHealth().getKafka().isEnabled()) {
      return HealthStatusDto.builder()
          .status("SKIPPED")
          .detail("Kafka health checks disabled by configuration")
          .build();
    }

    Map<String, Object> config =
        Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    try (AdminClient client = AdminClient.create(config)) {
      String clusterId = client.describeCluster().clusterId().get(3, TimeUnit.SECONDS);
      return HealthStatusDto.builder().status("UP").detail("ClusterId=" + clusterId).build();
    } catch (TimeoutException ex) {
      return HealthStatusDto.builder().status("DOWN").detail("Timed out contacting broker").build();
    } catch (ExecutionException | InterruptedException ex) {
      Thread.currentThread().interrupt();
      return HealthStatusDto.builder()
          .status("DOWN")
          .detail(ex.getMessage() == null ? "Kafka unavailable" : ex.getMessage())
          .build();
    } catch (Exception ex) {
      return HealthStatusDto.builder().status("DOWN").detail(ex.getMessage()).build();
    }
  }
}
