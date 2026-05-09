package com.chronos.UserService.utils;

import com.chronos.UserService.entity.EmailVerificationToken;
import com.chronos.UserService.entity.User;

public class GenerateVerificationToken {

    public static EmailVerificationToken generateToken(String email, User user) {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setToken(java.util.UUID.randomUUID().toString());
        token.setUser(user);
        token.setEmail(email);
        return token;
    }
}
