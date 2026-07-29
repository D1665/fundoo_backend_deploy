package com.fundoonotes.fundoo_notes.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String token);

    void sendPasswordResetEmail(String toEmail, String token);

    void sendReminderEmail(String toEmail, String noteTitle);

    void sendCollaboratorEmail(String toEmail, String ownerEmail, String noteTitle);
}
