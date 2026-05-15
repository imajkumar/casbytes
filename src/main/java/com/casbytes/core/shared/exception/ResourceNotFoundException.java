package com.casbytes.core.shared.exception;

import com.casbytes.core.shared.api.ErrorCodes;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ResourceNotFoundException extends BusinessException {

  private final String resourceType;
  private final UUID resourceId;

  public ResourceNotFoundException(String resource, UUID id) {
    super(ErrorCodes.NOT_FOUND, resource + " not found for id=" + id);
    this.resourceType = resource;
    this.resourceId = id;
  }

  public ResourceNotFoundException(String message) {
    super(ErrorCodes.NOT_FOUND, message);
    this.resourceType = null;
    this.resourceId = null;
  }

  public boolean hasTypedResource() {
    return resourceType != null && resourceId != null;
  }
}
