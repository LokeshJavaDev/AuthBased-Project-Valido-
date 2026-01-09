package io.service.valido.core.auth.impl;

import io.service.valido.authentication.JwtService;
import io.service.valido.authentication.passwordUtils;
import io.service.valido.commons.util.ServiceError;
import io.service.valido.core.auth.UserService;
import io.service.valido.core.email.impl.JavaEmailService;
import io.service.valido.core.otp.OtpService;
import io.service.valido.dao.auth.UserDao;
import io.service.valido.model.dto.*;
import jakarta.mail.MessagingException;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.thymeleaf.context.Context;
import jakarta.ws.rs.core.Response.Status;
import io.service.valido.model.User;
import io.vavr.control.Either;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

import static io.service.valido.authentication.passwordUtils.hashPassword;


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
            final var refreshToken = jwtService.generateRefreshToken(user.getId().toString());

            return Either.right(LoginResponseDto.from(user, token, refreshToken));
        }catch (IllegalArgumentException e){
            return Either.left(ServiceError.builder()
                    .code(Status.FORBIDDEN.getStatusCode())
                    .message("Invalid credentials")
                    .build()
            );
        }
    }

    @Override
    public Either<ServiceError, Map<String, Object>> getRefreshToken(String refreshToken) {
        try {
            final var userId = jwtService.extractClaim(refreshToken).getSubject();
            UUID uuid = UUID.fromString(userId);
            return userDao.retrieve(uuid).get()
                    .map(user -> {
                        final Map<String, Object> claims = getClaims(user);
                        final var token = jwtService.generateToken(claims,user);
                        Map<String, Object> result = new HashMap<>();
                        result.put("token",token);
                        result.put("refreshToken",refreshToken);
                        return Either.<ServiceError, Map<String, Object>>right(result);
                    }).orElse(Either.left(ServiceError.builder()
                            .code(Status.NOT_FOUND.getStatusCode())
                            .message("User not found")
                            .build()
                    ));
        } catch (Exception e) {
            return Either.left(ServiceError.builder()
                    .code(Status.FORBIDDEN.getStatusCode())
                    .build()
            );
        }
    }

    @Override
    public Either<ServiceError,SignupResponseDto> signup(SignupDto signupDto) {
        final var now = LocalDateTime.now();
        final var userId = UUID.randomUUID();

        Either<ServiceError, SignupResponseDto> txtResult = jdbi.inTransaction(handle ->  {
            return userDao.getUserByEmail(signupDto.getEmail())
                    .flatMap(optionalUser ->
                            optionalUser.isPresent()
                            ? Either.left(ServiceError.builder()
                                    .code(Status.CONFLICT.getStatusCode())
                                    .message("User email already exists")
                                    .build()
                            ):Either.right(optionalUser)
                    ).flatMap(user -> {
                        final var userEntity = User.builder()
                                .id(userId)
                                .lastName(signupDto.getLastName())
                                .firstName(signupDto.getFirstName())
                                .email(signupDto.getEmail())
                                .password(hashPassword(signupDto.getPassword()))
                                .phoneNumber(signupDto.getPhoneNumber())
                                .modifier(userId)
                                .creator(userId)
                                .createdAt(signupDto.getCreatedAt() != null ?
                                        signupDto.getCreatedAt() : now)
                                .updatedAt(signupDto.getCreatedAt() != null ?
                                        signupDto.getCreatedAt() : now)
                                .isVerified(false)
                                .isActive(false)
                                .build();

                        final var createUser = userDao.create(userEntity);
                        if(createUser.isEmpty()) {
                            return Either.left(ServiceError.builder()
                                    .code(Status.INTERNAL_SERVER_ERROR.getStatusCode())
                                    .message("User not created")
                                    .build()
                            );
                        }

                        final var response = SignupResponseDto.from(createUser.get());
                        return Either.right(response);
                    });
        });

        if (txtResult.isRight()){
            try{
                sendWelcomeOtp(txtResult.get().getEmail());
            }catch (Exception e){
                LOGGER.error("Failed to send welcome email", e);
                // Don't fail the signup, just log the error
            }
        }
        return  txtResult;
    }


    private Map<String, Object> getClaims(User user) {
        final var claims = new HashMap<String, Object>();
        claims.put("email", user.getEmail());
        claims.put("userId",user.getId());
        return claims;
    }

    private void sendWelcomeOtp(String toEmail) {
        String otp = otpService.generateOtp(toEmail);
        long expiresInMinutes = otpTtlSeconds / 60;

        Context ctx = new Context();
        ctx.setVariable("brand",brand);
        ctx.setVariable("otp", otp);
        ctx.setVariable("expiresInMinutes", expiresInMinutes);
        ctx.setVariable("year", Year.now().getValue());

        String htmlBody = templateEngine.process("welcome-onboarding", ctx);
        String subject = "Welcome to " + brand + " - Verify Your Email";

        try {
            javaEmailService.sendHtmlEmail(toEmail, subject, htmlBody);
        }catch (MessagingException e) {
            throw new RuntimeException("Failed to send onboarding email", e);
        }
    }

    @Override
    public Either<ServiceError, VerifyOtpResponseDto> verifySignupOtp(VerifyOtpRequestDto request) {
        final var userResult = userDao.getUserByEmail(request.getEmail());

        if(userResult.isLeft()){
            return Either.left(userResult.getLeft());
        }

        final Optional<User> userOpt = userResult.get();
        if(userOpt.isEmpty()) {
            return Either.left(ServiceError.builder()
                    .code(Status.NOT_FOUND.getStatusCode())
                    .message("User not Found")
                    .build());
        }

        final User user = userOpt.get();
        if(user.isVerified()) {
            return Either.right(VerifyOtpResponseDto.builder()
                            .verified(true)
                            .message("User already verified")
                            .build()
            );
        }

        boolean ok = otpService.verifyOtp(request.getEmail(), request.getOtp());

        if(!ok) {
            return Either.left(new ServiceError(401, "Invalid or expired OTP"));
        }

        try {
            userDao.markUserVerified(user.getId());
        }catch (UnableToExecuteStatementException e){
            LOGGER.error("Failed to mark user verified: {}", e.getMessage(), e);
            return Either.left(new ServiceError(500, "Failed to update user verification state"));
        }

        return Either.right(VerifyOtpResponseDto.builder()
                .verified(true)
                .message("Email verified successfully")
                .build()
        );


    }


}
