package io.service.valido.model.dto;

import io.service.valido.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private String message;

    public static SignupResponseDto from(User user) {
        return SignupResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .message("Signup successful. Please verify your email with the OTP sent.")
                .build();
    }
}