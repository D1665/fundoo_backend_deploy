package com.fundoonotes.fundoo_notes.service;

import com.fundoonotes.fundoo_notes.dto.LoginDTO;
import com.fundoonotes.fundoo_notes.dto.UserDTO;

public interface UserService {

    String register(UserDTO dto);

    // OLD token based - keep for backward compatibility
    String verifyEmail(String token);

    String login(LoginDTO dto);

    String forgotPassword(String email);

    String resetPassword(String token, String newPassword);

    // LOGOUT — blacklists the current JWT in Redis
    String logout(String token);
}