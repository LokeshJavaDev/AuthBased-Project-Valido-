package io.service.valido.core.auth.impl;

import io.service.valido.authentication.JwtService;
import io.service.valido.authentication.passwordUtils;
import io.service.valido.commons.util.ServiceError;
import io.service.valido.core.auth.UserService;
import io.service.valido.core.email.impl.JavaEmailService;
import io.service.valido.core.otp.OtpService;
import io.service.valido.dao.auth.UserDao;
import io.service.valido.model.dto.LoginResponseDto;
import jakarta.ws.rs.core.Response.Status;
import io.service.valido.model.User;
import io.vavr.control.Either;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class UserServiceImpl  implements UserService {
    private static  final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);


    private final UserDao userDao;
    private final JwtService jwtService;
    private final JavaEmailService javaEmailService;
    private final OtpService otpService;
    private final TemplateEngine templateEngine;
    private final Jdbi jdbi;


    @Value("${app.brand:Valido}")
    private String brand;

    @Value("${otp.ttl-seconds:1800}")
    private long otpTtlSeconds;


    public UserServiceImpl(
            final UserDao dao,
            final JwtService jwtService,
            final JavaEmailService javaEmailService,
            final OtpService otpService,
            final TemplateEngine templateEngine,
            final Jdbi jdbi) {
        this.userDao = dao;
        this.jwtService = jwtService;
        this.javaEmailService = javaEmailService;
        this.otpService = otpService;
        this.templateEngine = templateEngine;
        this.jdbi = jdbi;
    }

    @Override
    public Either<ServiceError, List<User>>list() {
        final var  users = userDao.fetch().get();
        if(users.isEmpty()) {
            return Either.left(ServiceError.builder()
                    .code(Status.NOT_FOUND.getStatusCode())
                    .message("No  users found")
                    .build()
            );
        }
        return Either.right(users);
    }

    @Override
    public Either<ServiceError, User> retrive(UUID id){
        final var user = userDao.retrieve(id).get();
        if(user.isPresent()) {
            return Either.right(user.get());
        }
        return Either.left(ServiceError.builder()
                .code(Status.NOT_FOUND.getStatusCode())
                .message("User not found")
                .build()
        );
    }

    @Override
    public Either<ServiceError, LoginResponseDto> login(Map<String, String> credentials) {
        final var email = credentials.get("email");
        final var password = credentials.get("password");

        if(email == null || password == null) {
            return Either.left(ServiceError.builder()
                    .code(Status.BAD_REQUEST.getStatusCode())
                    .message("Invalid credentials")
                    .build()
            );
        }

        try{
            final var user = userDao.getUserByCredentials(email,"");
            if(user == null || !passwordUtils.matches(password, user.getPassword())) {
                return Either.left(ServiceError.builder()
                        .code(Status.BAD_REQUEST.getStatusCode())
                        .message("Invalid credentials")
                        .build()
                );
            }

            if(!user.isVerified()) {
                return Either.left(ServiceError.builder()
                        .code(Status.FORBIDDEN.getStatusCode())
                        .message("Email not verified. Please verify your email first.")
                        .build()
                );
            }

            if(!user.isActive()) {
                return Either.left(ServiceError.builder()
                        .code(Status.FORBIDDEN.getStatusCode())
                        .message("Account is inactive. Please contact support.")
                        .build()
                );
            }

            final var claims = getClaims(user);
            final var token = jwtService.generateToken(claims, user);
            final var refreshToken = jwtService.generateRefreshToken(user.getId());

            return Either.right(LoginResponseDto.from(user, token, refreshToken));
        }catch (IllegalArgumentException e){
            return Either.left(ServiceError.builder()
                    .code(Status.FORBIDDEN.getStatusCode())
                    .message("Invalid credentials")
                    .build()
            );
        }
    }


    private Map<String, Object> getClaims(User user) {
        final var claims = new HashMap<String, Object>();
        claims.put("email", user.getEmail());
        claims.put("userId",user.getId());
        return claims;
    }


}
