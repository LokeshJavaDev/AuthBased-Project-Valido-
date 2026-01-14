package io.service.valido.spring.configuration;

import io.service.valido.authentication.JwtService;
import io.service.valido.core.auth.impl.UserServiceImpl;
import io.service.valido.core.email.impl.JavaEmailService;
import io.service.valido.core.otp.OtpService;
import io.service.valido.core.otp.impl.OtpServiceImpl;
import io.service.valido.dao.auth.UserDao;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;

@Configuration
public class ServiceConfiguration {

    @Bean
    public UserServiceImpl userService(
            final UserDao userDao,
            final JwtService jwtService,
            final JavaEmailService javaEmailService,
            final OtpService otpService,
            final TemplateEngine templateEngine,
            final Jdbi jdbi) {
        return new UserServiceImpl(userDao, jwtService, javaEmailService,
                otpService, templateEngine, jdbi);
    }

    @Bean
    public OtpServiceImpl otpService() {
        return new OtpServiceImpl();
    }
}