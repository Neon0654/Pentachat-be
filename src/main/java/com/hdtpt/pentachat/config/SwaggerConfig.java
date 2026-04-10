package com.hdtpt.pentachat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI pentachatOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PentaChat API Documentation")
                        .description("Backend API documentation for PentaChat Messaging Platform")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("PentaChat Team")
                                .email("support@pentachat.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
