package io.service.valido.model.dto;

import io.service.valido.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class LoginResponseDto {
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String token;
    private String refreshToken;

    public static LoginResponseDto from(User user, String token, String refreshToken) {
        return LoginResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())

                .build();
    }


}
