package com.mediq.config;

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
public class OpenApiConfig {

    @Bean
    public OpenAPI mediqOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("https://medical-camp-project-production.up.railway.app").description("Production Environment (Railway)"),
                        new Server().url("http://localhost:8080").description("Local Development Server (8080)"),
                        new Server().url("http://localhost:8081").description("Local Development Server (8081)")
                ))
                .info(new Info()
                        .title("MediQ – Digital Prescription & Medical Camp Management System API")
                        .description("Production-ready RESTful APIs for complete medical camp lifecycle management including User Admin, Camp Setup, Patient Registration, Queue Engine, Nurse Vitals, Doctor Consultations, Digital Prescriptions, Pharmacy Dispensing, and Hospital Referrals.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MediQ Engineering Team")
                                .email("support@mediq.health"))
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
