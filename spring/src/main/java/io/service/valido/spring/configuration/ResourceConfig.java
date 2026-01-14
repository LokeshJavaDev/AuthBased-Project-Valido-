package io.service.valido.spring.configuration;

import io.service.valido.core.auth.UserService;
import io.service.valido.rest.auth.AuthResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResourceConfig {
    @Bean
    public AuthResource authResource(final UserService userService) {
        return new AuthResource(userService);
    }

}
