package com.casbytes.core.modules.reference.mapper;

import com.casbytes.core.modules.reference.domain.ReferenceItem;
import com.casbytes.core.modules.reference.dto.ReferenceItemCreateRequest;
import com.casbytes.core.modules.reference.dto.ReferenceItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReferenceItemMapper {

  ReferenceItemResponse toResponse(ReferenceItem entity);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "active", constant = "true")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  ReferenceItem toNewEntity(ReferenceItemCreateRequest request);
}
