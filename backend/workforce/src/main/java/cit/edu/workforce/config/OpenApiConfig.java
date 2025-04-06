package cit.edu.workforce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("WorkforceHub HR Management System API")
                        .description("API documentation for WorkforceHub HR Management System application. " +
                                "This API provides endpoints for authentication, employee management, certification tracking, " +
                                "and document management.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("WorkforceHub Team")
                                .email("support@workforcehub.com")
                                .url("https://workforcehub.com"))
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .tags(createTagList());
    }
    
    private List<Tag> createTagList() {
        return Arrays.asList(
            new Tag().name("Authentication").description("Authentication operations including login, registration, and token refresh"),
            new Tag().name("Employee Management").description("Operations for managing employees, including CRUD operations"),
            new Tag().name("Document Management").description("Operations for managing employee documents and certifications"),
            new Tag().name("Certification Management").description("Operations for managing employee certifications"),
            new Tag().name("Role Management").description("Operations for managing roles and permissions"),
            new Tag().name("Department Management").description("Operations for managing departments"),
            new Tag().name("Job Title Management").description("Operations for managing job titles"),
            new Tag().name("Email Domain Management").description("Operations for managing allowed email domains")
        );
    }
} 