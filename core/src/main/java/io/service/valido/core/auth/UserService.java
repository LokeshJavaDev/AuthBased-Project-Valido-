package io.service.valido.core.auth;

import io.service.valido.commons.util.ServiceError;
import io.service.valido.core.CRUDService;
import io.service.valido.model.User;
import io.service.valido.model.dto.LoginResponseDto;
import io.service.valido.model.dto.SignupDto;
import io.service.valido.model.dto.SignupResponseDto;
import io.service.valido.model.dto.VerifyOtpRequestDto;
import io.service.valido.model.dto.VerifyOtpResponseDto;
import io.vavr.control.Either;

import java.util.Map;
import java.util.UUID;

public interface UserService extends CRUDService<User, UUID> {
    Either<ServiceError, LoginResponseDto> login(Map<String, String> credentials);
    Either<ServiceError, Map<String, Object>> getRefreshToken(String refreshToken);
    Either<ServiceError, SignupResponseDto> signup(SignupDto signupDto);
    Either<ServiceError, VerifyOtpResponseDto> verifySignupOtp(VerifyOtpRequestDto request);
}