package com.example.autoauction.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Auto Auction API")
                        .version("1.0.0")
                        .description("REST API для системы автомобильных аукционов\n\n" +
                                "### Модули:\n" +
                                "* **User** - управление пользователями\n" +
                                "* **Auction** - управление аукционами\n" +
                                "* **Vehicle** - управление транспортными средствами\n" +
                                "* **Deposit** - управление депозитами\n" +
                                "* **Admin** - административные функции")
                        .contact(new Contact()
                                .name("Temirlan Alikovich")
                                .email("your.email@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите JWT токен. Пример: Bearer eyJhbGciOiJIUzI1NiJ9...")));
    }
}