package io.service.valido.core.otp;

public interface OtpService {
    String generateOtp(String email);
    boolean verifyOtp(String email, String Otp);
}
