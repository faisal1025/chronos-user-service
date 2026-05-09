package com.chronos.UserService.repository;

import com.chronos.UserService.entity.EmailVerificationToken;
import com.chronos.UserService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationTokenRepo extends JpaRepository<EmailVerificationToken, Long> {
    boolean existsByUser(User user);

    void deleteByUser(User user);

    EmailVerificationToken findByToken(String token);
}
