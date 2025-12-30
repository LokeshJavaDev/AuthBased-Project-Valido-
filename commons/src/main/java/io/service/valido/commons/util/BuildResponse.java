package io.service.valido.commons.util;

import lombok.Builder;
import lombok.Value;
import jakarta.ws.rs.core.Response;

@Value
@Builder(toBuilder = true)

public class BuildResponse <T>{
    int statusCode;
    int message;
    T Data;

    public Response toResponse() {
        return Response.status(statusCode)
                .entity(this)
                .build();
    }
}
