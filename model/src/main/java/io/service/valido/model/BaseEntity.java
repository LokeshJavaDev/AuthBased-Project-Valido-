package io.service.valido.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;import java.util.UUID;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)

public abstract class BaseEntity implements HasIdentity<UUID>{
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID creator;
    private UUID modifier;
}
