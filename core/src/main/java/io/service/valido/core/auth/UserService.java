package io.service.valido.core.auth;

import io.service.valido.commons.util.ServiceError;
import io.service.valido.core.CRUDService;
import io.service.valido.model.dto.*;
import io.vavr.control.Either;

import java.util.Map;

public interface UserService extends CRUDService {
    Either<ServiceError, LoginResponseDto> login(Map<String, String> credentials);
    Either<ServiceError, Map<String, Object>> getRefreshToken(String refreshToken);
    Either<ServiceError, SignupResponseDto> signup(SignupDto signupDto);
    Either<ServiceError, VerifyOtpResponseDto> verifySignupOtp(VerifyOtpRequestDto request);

}
