package io.service.valido.spring.configuration;

import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}