package com.casbytes.core.shared.exception;

import com.casbytes.core.shared.api.ErrorCodes;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

  private final String code;

  public BusinessException(String code, String message) {
    super(message);
    this.code = code;
  }

  public BusinessException(String message) {
    this(ErrorCodes.BUSINESS_RULE_VIOLATION, message);
  }
}
