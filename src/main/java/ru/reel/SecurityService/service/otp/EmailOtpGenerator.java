package ru.reel.SecurityService.service.otp;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class EmailOtpGenerator implements OtpGenerator {
    @Override
    public String generate(int size) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for(int i = 0; i < size; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
