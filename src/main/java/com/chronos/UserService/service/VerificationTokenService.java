package com.chronos.UserService.service;

import com.chronos.UserService.entity.EmailVerificationToken;
import com.chronos.UserService.entity.User;
import org.springframework.stereotype.Service;

@Service
public class VerificationTokenService {

    public EmailVerificationToken generateToken(String email, User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(java.util.UUID.randomUUID().toString());
        token.setUser(user);
        token.setEmail(email);
        return token;
    }
}
