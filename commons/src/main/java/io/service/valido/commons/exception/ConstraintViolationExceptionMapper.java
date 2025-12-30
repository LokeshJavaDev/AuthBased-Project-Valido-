package io.service.valido.commons.exception;

import io.service.valido.commons.util.ServiceError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ServiceError> errors = exception.getConstraintViolations().stream()
                .map(this::toServiceError)
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errors)
                .build();
    }

    private ServiceError toServiceError(ConstraintViolation<?> violation) {
        final var message = violation.getMessage();

        return ServiceError.builder()
                .code(400)
                .message(message)
                .timeStamp(Instant.now())
                .build();
    }

}


