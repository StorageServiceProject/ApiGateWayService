package com.brodep.apigatewayservice.configuration.beans;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "s3")
@Component
@Getter
@Setter
public class MinioProperties {
    private String host;
    private String bucketName;
    private String username;
    private String password;
}
