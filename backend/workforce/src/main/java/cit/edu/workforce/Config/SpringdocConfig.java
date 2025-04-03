package cit.edu.workforce.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition
public class SpringdocConfig {

    @Bean
    public SpringDocConfigProperties springDocConfigProperties() {
        SpringDocConfigProperties properties = new SpringDocConfigProperties();
        // Disable internationalization which could be related to the error
        properties.setDisableI18n(true);
        return properties;
    }
} 