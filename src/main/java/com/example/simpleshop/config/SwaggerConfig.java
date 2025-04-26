package com.example.simpleshop.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new io.swagger.v3.oas.models.info.Info()
                    .title("SimpleShop API")
                    .version("v1")
                    .description("세션 기반 쇼핑몰 API"))
            .addSecurityItem(new SecurityRequirement().addList("JSESSIONID"))
            .components(new Components().addSecuritySchemes("JSESSIONID",
                    new SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .in(SecurityScheme.In.COOKIE)
                            .name("JSESSIONID")
            ));
    }
}
