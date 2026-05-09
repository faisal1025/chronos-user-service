package com.chronos.UserService.service;

import com.chronos.UserService.dto.UserDto;
import com.chronos.UserService.entity.EmailVerificationToken;
import com.chronos.UserService.entity.User;
import com.chronos.UserService.exception.UnauthorizedUserException;
import com.chronos.UserService.messaging.NotifyEmailMessage;
import com.chronos.UserService.repository.EmailVerificationTokenRepo;
import com.chronos.UserService.repository.UserRepository;
import com.chronos.UserService.utils.GenerateVerificationToken;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class UserManagementService {
    private final String topic;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepo emailVerificationTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final KafkaMessageService kafkaMessageService;
    private final VerificationTokenService verificationTokenService;
    public UserManagementService(@Value("${kafka.topic.name:notify_email}") String topic,
                                 UserRepository userRepository,
                                 EmailVerificationTokenRepo emailVerificationTokenRepo,
                                 PasswordEncoder paaswordEncoder,
                                 KafkaMessageService kafkaMessageService,
                                 VerificationTokenService verificationTokenService) {
        this.kafkaMessageService = kafkaMessageService;
        this.userRepository = userRepository;
        this.emailVerificationTokenRepo = emailVerificationTokenRepo;
        this.passwordEncoder = paaswordEncoder;
        this.verificationTokenService = verificationTokenService;
        this.topic = topic;
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }

        return userToUserDtoMapper(user);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with email: " + email + " not found");
        }

        return userToUserDtoMapper(user);
    }

    public UserDto updateUser(Long id, String email, String oldPassword, String newPassword) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }
        if(!user.isVerified()){
            throw new UnauthorizedUserException("User email is not verified. Please verify your email before updating to a new email.");
        }

        this.updateUserEmailRequest(user, email);

        this.updateUserPassword(user, oldPassword, newPassword);
        User updatedUser = userRepository.save(user);

        return userToUserDtoMapper(updatedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, String newName){
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + id + " not found");
        }

        user.setName(newName);
        User updatedUser = userRepository.save(user);

        return userToUserDtoMapper(updatedUser);
    }

    @Transactional
    public EmailVerificationToken updateUserEmailRequest(User user, String newEmail) {

        EmailVerificationToken verificationToken = verificationTokenService.generateToken(newEmail, user);
        if(emailVerificationTokenRepo.existsByUser(user)){
            emailVerificationTokenRepo.deleteByUser(user);
        }
        EmailVerificationToken savedToken = emailVerificationTokenRepo.save(verificationToken);

        // send to kafka topic for email sending
        kafkaMessageService.sendMessage(topic, newEmail, new NotifyEmailMessage(newEmail, savedToken.getToken()));
        return savedToken;
    }

    @Transactional
    public EmailVerificationToken updateUserEmailRequest(Long userId, String newEmail) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + userId + " not found");
        }
        if(!user.isVerified()){
            throw new UnauthorizedUserException("User email is not verified. Please verify your email before updating to a new email.");
        }

        EmailVerificationToken verificationToken = verificationTokenService.generateToken(newEmail, user);
        if(emailVerificationTokenRepo.existsByUser(user)){
            emailVerificationTokenRepo.deleteByUser(user);
        }
        EmailVerificationToken savedToken = emailVerificationTokenRepo.save(verificationToken);

        // send to kafka topic for email sending
        kafkaMessageService.sendMessage(topic, "verification-email", savedToken);
        return savedToken;
    }

    public boolean updateUserPassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UnauthorizedUserException("Old password is incorrect");
        }

        user.setPassword(newPassword);
        return true;
    }

    @Transactional
    public UserDto updateUserPassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + userId + " not found");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UnauthorizedUserException("Old password is incorrect");
        }

        user.setPassword(newPassword);
        User savedUser = userRepository.save(user);
        return userToUserDtoMapper(savedUser);
    }

    @Transactional
    public UserDto verifyTokenAndUpdateEmail(Long userId, String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepo.findByToken(token);
        if (verificationToken == null) {
            throw new EntityNotFoundException("Invalid verification token");
        }

        User user = verificationToken.getUser();
        if(!user.getId().equals(userId)){
            throw new UnauthorizedUserException("User ID does not match the token's user");
        }
        user.setEmail(verificationToken.getEmail());
        userRepository.save(user);
        emailVerificationTokenRepo.delete(verificationToken);

        return userToUserDtoMapper(user);
    }

    private UserDto userToUserDtoMapper(User user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.isVerified(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
