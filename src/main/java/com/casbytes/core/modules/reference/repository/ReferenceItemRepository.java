package com.casbytes.core.modules.reference.repository;

import com.casbytes.core.modules.reference.domain.ReferenceItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferenceItemRepository extends JpaRepository<ReferenceItem, UUID> {

  Optional<ReferenceItem> findByCodeIgnoreCase(String code);

  boolean existsByCodeIgnoreCase(String code);
}
