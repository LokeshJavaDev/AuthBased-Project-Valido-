package io.service.valido.rest.auth;


import io.service.valido.core.auth.UserService;
import io.service.valido.model.dto.SignupDto;
import io.service.valido.model.dto.VerifyOtpRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.print.attribute.standard.Media;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    private final UserService service;

    public AuthResource(UserService service) {
        this.service = service;
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)

    public Response login(final Map<String, String> userDetails) {
        final var result = service.login(userDetails);
        return result.fold(
                error -> Response.status(error.getCode())
                        .entity(error)
                        .build(),
                user -> Response.ok().build()
        );
    }

    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRefreshToken(@HeaderParam("Authorization") String refreshToken) {
        final var result = service.getRefreshToken(refreshToken);
        return result.fold(
                error -> Response.status(error.getCode())
                        .entity(error)
                        .build(),
                user -> Response.ok(user).build()
        );
    }

    @POST
    @Path("/signup")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response signup(@Valid @NotNull SignupDto signupDto) {
        final var result = service.signup(signupDto);
        return result.fold(
                error -> Response.status(error.getCode())
                        .entity(error)
                        .build(),
                created -> Response.ok(created).build()
        );
    }

    @POST
    @Path("/verify-otp")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifySignupOtp(@Valid @NotNull VerifyOtpRequestDto request) {
        final var result = service.verifySignupOtp(request);
        return result.fold(
                error -> Response.status(error.getCode())
                        .entity(error)
                        .build(),
                created -> Response.ok(created).build()
        );
    }
}
