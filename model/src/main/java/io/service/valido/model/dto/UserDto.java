package io.service.valido.model.dto;

import io.service.valido.model.BaseEntity;
import io.service.valido.model.User;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)

public class UserDto extends BaseEntity {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private boolean isActive;
    private boolean isVerified;
    private String password;


    // Entity ----> DTO
    public static UserDto from(User user){
        if (user == null)
            return null;

        return UserDto.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .creator(user.getCreator())
                .modifier(user.getModifier())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    // DTO -------> Entity

    public static User to(UserDto userDto) {
        if(userDto == null)
            return null;

        return User.builder()
                .id(userDto.getId())
                .createdAt(userDto.getCreatedAt())
                .updatedAt(userDto.getUpdatedAt())
                .creator(userDto.getCreator())
                .modifier(userDto.getModifier())
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .phoneNumber(userDto.getPhoneNumber())
                .isActive(userDto.isActive())
                .isVerified(userDto.isVerified())
                .build();
    }
}
