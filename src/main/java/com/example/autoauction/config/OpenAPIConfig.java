package com.example.autoauction.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auto Auction API")
                        .version("1.0.0")
                        .description("REST API для системы автомобильных аукционов\n\n" +
                                "Модули:\n" +
                                "* **User** - управление пользователями\n" +
                                "* **Auction** - управление аукционами\n" +
                                "* **Vehicle** - управление транспортными средствами\n" +
                                "* **Deposit** - управление депозитами\n" +
                                "* **Admin** - административные функции")
                        .contact(new Contact()
                                .name("Temirlan Alikovich")
                                .email("timanalikovich@gmail.com")
                                .url("https://empty.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server"),
                        new Server()
                                .url("https://api.autoauction.com")
                                .description("Production server")
                ));
    }
}