package io.service.valido.core.otp.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.service.valido.core.otp.OtpService;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OtpServiceImpl implements OtpService {
    private static  final SecureRandom random = new SecureRandom();

    private static final long RESEND_INTERVAL_MILLIS = TimeUnit.MINUTES.toMillis(1);


    private final Cache<String, Map<String, Object>> otpCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .maximumSize(10_000)
            .build();



    @Override
    public String generateOtp(String email) {
        long now = System.currentTimeMillis();

        Map<String, Object> existing = otpCache.getIfPresent(email);

        if (existing != null) {
            long lastSent = (long) existing.get("timestamp");

            if (now - lastSent < RESEND_INTERVAL_MILLIS) {
                long remaining = (RESEND_INTERVAL_MILLIS - (now - lastSent)) / 1000;

                throw new IllegalStateException("Please wait " + remaining + " seconds before resending OTP.");
            }
        }

        final var otp = String.format("%06d", random.nextInt(999999));

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("timestamp", now);
        otpCache.put(email, otpData);

        return otp;
    }

    @Override
     public  boolean verifyOtp(String email, String otp){
        Map<String, Object> otpData = otpCache.getIfPresent(email);

        if(otpData == null) {
            return false;
        }
        final var savedOtp = (String)otpData.get("otp");
        if(savedOtp.equals(otp)) {
            otpCache.invalidate(email);
            return true;
        }
        return false;
    }
}
