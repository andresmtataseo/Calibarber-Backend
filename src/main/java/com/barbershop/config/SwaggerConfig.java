package com.barbershop.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "API REST for the Barbershop",
                description = "Barbershop API REST documentation",
                termsOfService = "Terms of service",
                contact = @Contact(
                        name = "ANDRES MORENO",
                        url = "https://github.com/andresmtataseo",
                        email = "andresmoreno2001@gmail.com"
                ),
                license =  @License(
                        name = "License",
                        url = "https://github.com/andresmtataseo/barbershop-backend/blob/master/LICENSE"
                ),
                version = "1.0.0"),
        servers = {
                @Server(url = "http://localhost:8080", description = "Development server"),
                @Server(url = "https://barbershop-backend.herokuapp.com", description = "Production server")
        },
        security = @SecurityRequirement(
                name = "Security Token",
                scopes = {}
        )
)
@SecurityScheme(
        name = "Security Token",
        description = "Access token for API REST",
        paramName = HttpHeaders.AUTHORIZATION,
        in = SecuritySchemeIn.HEADER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
