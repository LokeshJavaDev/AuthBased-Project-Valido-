package io.service.valido.spring.configuration;

import io.service.valido.authentication.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {

    private static final String SECRET = "mA4D6SZrT3kZ5JcHcVuVt3a2cWn+6A1yFpoFqE3VtFQ=";
    private static final long JWT_EXPIRATION = 24 * 60 * 60 * 1000L; // 24 hours
    private static final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7 days

    @Bean
    public JwtService jwtService() {
        return new JwtService(SECRET, JWT_EXPIRATION, REFRESH_EXPIRATION);
    }
}