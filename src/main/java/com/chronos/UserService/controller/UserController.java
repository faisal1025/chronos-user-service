package com.chronos.UserService.controller;

import com.chronos.UserService.dto.UserDto;
import com.chronos.UserService.service.UserManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserManagementService userManagementService;

    public UserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        UserDto user = userManagementService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDto> getByEmail(@RequestBody String email) {
        UserDto user = userManagementService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/name")
    public ResponseEntity<UserDto> updateUserName(@PathVariable Long id, @RequestBody UpdateNameRequest request) {
        UserDto updated = userManagementService.updateUser(id, request.getName());
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/email")
    public ResponseEntity<Void> updateUserEmail(@PathVariable Long id, @RequestBody UpdateEmailRequest request) {
        // This triggers a verification token to be generated and sent — response acknowledges the request
        userManagementService.updateUserEmailRequest(id, request.getNewEmail());
        return ResponseEntity.accepted().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UserDto> updatePassword(@PathVariable Long id, @RequestBody UpdatePasswordRequest request) {
        UserDto updated = userManagementService.updateUserPassword(id, request.getOldPassword(), request.getNewPassword());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/verify-email")
    public ResponseEntity<UserDto> verifyCodeAndUpdateEmail(@PathVariable Long id, @RequestBody VerifyAndUpdateEmailRequest request) {
        UserDto updated = userManagementService.verifyTokenAndUpdateEmail(id, request.getCode());
        return ResponseEntity.ok(updated);
    }

    // Simple request DTOs used by the endpoints
    public static class UpdateNameRequest {
        private String name;

        public UpdateNameRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class UpdateEmailRequest {
        private String newEmail;

        public UpdateEmailRequest() {}

        public String getNewEmail() { return newEmail; }
        public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    }

    public static class UpdatePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public UpdatePasswordRequest() {}

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class VerifyAndUpdateEmailRequest {
        private String code;

        public VerifyAndUpdateEmailRequest() {}

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

}
