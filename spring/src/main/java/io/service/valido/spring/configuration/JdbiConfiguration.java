package io.service.valido.spring.configuration;

import io.service.valido.dao.auth.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JdbiConfiguration {

    @Bean
    UserDao userDao(Jdbi jdbi) {
        return new UserDao(jdbi);
    }
}