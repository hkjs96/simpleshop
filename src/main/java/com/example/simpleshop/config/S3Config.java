package com.example.simpleshop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2) // 서울
                .credentialsProvider(DefaultCredentialsProvider.create()) // IAM Role 기반
                .build();
    }

    @Bean
    public String s3Bucket() {
        return bucket;
    }
}

