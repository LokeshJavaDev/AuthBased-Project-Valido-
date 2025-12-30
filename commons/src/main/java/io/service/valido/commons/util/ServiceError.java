package io.service.valido.commons.util;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
@AllArgsConstructor

public class ServiceError {
    int code;

    String message;

    @Nullable
    Instant timeStamp;

    public ServiceError(int code, String message) {
        this.code = code;
        this.message = message;
        this.timeStamp = getTimeStamp();

    }
}
