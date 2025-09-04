package com.brodep.apigatewayservice.exeption;

public class SendFailedKafkaException extends RuntimeException {
  public SendFailedKafkaException(String message) {
    super(message);
  }
}
