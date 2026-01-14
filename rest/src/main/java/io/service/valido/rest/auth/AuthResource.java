package io.service.valido.rest.auth;

import io.service.valido.core.auth.UserService;
import io.service.valido.model.dto.SignupDto;
import io.service.valido.model.dto.VerifyOtpRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Path("/auth")
public class AuthResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthResource.class);
    private final UserService service;

    public AuthResource(UserService service) {
        this.service = service;
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(final Map<String, String> userDetails) {
        LOGGER.info("Login endpoint called");

        if (userDetails == null) {
            LOGGER.error("userDetails is null");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Invalid request body"))
                    .build();
        }

        LOGGER.info("Login attempt for email: {}", userDetails.get("email"));

        final var result = service.login(userDetails);

        return result.fold(
                error -> {
                    LOGGER.error("Login failed: {} - {}", error.getCode(), error.getMessage());
                    return Response.status(error.getCode())
                            .entity(error)
                            .build();
                },
                loginResponse -> {
                    return Response.ok(loginResponse).build();
                }
        );
    }

    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRefreshToken(@HeaderParam("Authorization") String refreshToken) {
        LOGGER.info("Refresh token endpoint called");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            LOGGER.error("Refresh token is missing");
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Refresh token is required"))
                    .build();
        }

        final var result = service.getRefreshToken(refreshToken);

        return result.fold(
                error -> {
                    LOGGER.error("Token refresh failed: {} - {}", error.getCode(), error.getMessage());
                    return Response.status(error.getCode())
                            .entity(error)
                            .build();
                },
                tokens -> {
                    LOGGER.info("Token refresh successful");
                    return Response.ok(tokens).build();
                }
        );
    }

    @POST
    @Path("/signup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signup(@Valid @NotNull SignupDto signupDto) {
        LOGGER.info("Signup endpoint called for email: {}", signupDto.getEmail());

        final var result = service.signup(signupDto);

        return result.fold(
                error -> {
                    LOGGER.error("Signup failed: {} - {}", error.getCode(), error.getMessage());
                    return Response.status(error.getCode())
                            .entity(error)
                            .build();
                },
                created -> {
                    LOGGER.info("Signup successful for email: {}", created.getEmail());
                    return Response.ok(created).build();
                }
        );
    }

    @POST
    @Path("/verify-otp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifySignupOtp(@Valid @NotNull VerifyOtpRequestDto request) {
        LOGGER.info("Verify OTP endpoint called for email: {}", request.getEmail());

        final var result = service.verifySignupOtp(request);

        return result.fold(
                error -> {
                    LOGGER.error("OTP verification failed: {} - {}", error.getCode(), error.getMessage());
                    return Response.status(error.getCode())
                            .entity(error)
                            .build();
                },
                response -> {
                    LOGGER.info("OTP verification successful for email: {}", request.getEmail());
                    return Response.ok(response).build();
                }
        );
    }
}