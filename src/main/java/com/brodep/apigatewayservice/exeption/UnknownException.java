package com.brodep.apigatewayservice.exeption;

public class UnknownException extends RuntimeException {
  public UnknownException(String message) {
    super(message);
  }
}
