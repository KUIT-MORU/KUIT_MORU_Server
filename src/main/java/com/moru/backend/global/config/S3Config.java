package com.moru.backend.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.aws.credentials", name = "access-key", matchIfMissing = false)  // ← 변경됨
public class S3Config {
    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // S3 서비스의 세부 설정을 구성합니다.
        // pathStyleAccessEnabled: 버킷 이름에 '.'이 포함될 때 발생하는 SSL 오류를 방지합니다.
        S3Configuration s3Configuration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();

        return S3Client.builder()
                .region(Region.of(region))
                // [추천] 엔드포인트를 명시적으로 지정하여 리전 문제를 근본적으로 해결합니다.
                .endpointOverride(URI.create("https://s3." + region + ".amazonaws.com"))
                .serviceConfiguration(s3Configuration)
                .build();
    }
}
