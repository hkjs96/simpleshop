package com.example.simpleshop.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "SimpleShop API", version = "v1", description = "세션 기반 쇼핑몰 API 문서")
)
public class SwaggerConfig {


}
