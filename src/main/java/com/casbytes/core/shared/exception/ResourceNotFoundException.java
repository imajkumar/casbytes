package com.casbytes.core.shared.exception;

import com.casbytes.core.shared.api.ErrorCodes;
import java.util.UUID;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, UUID id) {
        super(ErrorCodes.NOT_FOUND, resource + " not found for id=" + id);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCodes.NOT_FOUND, message);
    }
}
