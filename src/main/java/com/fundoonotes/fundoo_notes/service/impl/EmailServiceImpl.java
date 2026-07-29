package com.fundoonotes.fundoo_notes.service.impl;

import com.fundoonotes.fundoo_notes.service.EmailService;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.brevo.api-key:}")
    private String brevoApiKey;

    @Value("${app.resend.api-key:}")
    private String resendApiKey;

    @Value("${app.resend.from:onboarding@resend.dev}")
    private String resendFrom;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String link = backendUrl + "/api/users/verify?token=" + token;
        sendEmail(toEmail,
                "Verify Your Fundoo Notes Account",
                "Hello,\n\nClick to verify your account:\n\n"
                        + link + "\n\nThis link expires in 24 hours.\n\n"
                        + "Regards,\nFundoo Notes Team");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String token) {

        String link = frontendUrl + "/reset-password?token=" + token;

        String html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Reset Your Password</title>
</head>

<body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,Helvetica,sans-serif;">

<table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
<tr>
<td align="center">

<table width="600" cellpadding="0" cellspacing="0"
style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.15);">

<tr>
<td style="background:#4285F4;padding:25px;text-align:center;">

<h1 style="margin:0;color:white;font-size:30px;">
📝 Fundoo Notes
</h1>

</td>
</tr>

<tr>
<td style="padding:40px;">

<h2 style="margin-top:0;color:#333;text-align:center;">
Reset Your Password
</h2>

<p style="font-size:16px;color:#555;line-height:28px;">
Hello,
</p>

<p style="font-size:16px;color:#555;line-height:28px;">
We received a request to reset the password for your
<strong>Fundoo Notes</strong> account.
</p>

<p style="font-size:16px;color:#555;line-height:28px;">
Click the button below to create a new password.
</p>

<div style="text-align:center;margin:35px 0;">

<a href="%s"
style="
background:#4285F4;
color:white;
text-decoration:none;
padding:15px 35px;
font-size:16px;
font-weight:bold;
border-radius:6px;
display:inline-block;">
Reset Password
</a>

</div>

<div style="background:#f8f9fa;border-left:4px solid #4285F4;padding:15px;border-radius:5px;">

<p style="margin:0;font-size:14px;color:#555;">
⏰ <strong>This link is valid for 24 hours.</strong>
</p>

</div>

<p style="margin-top:30px;font-size:15px;color:#666;line-height:24px;">
If you didn't request a password reset, you can safely ignore this email.
Your password will remain unchanged.
</p>

<hr style="margin:35px 0;border:none;border-top:1px solid #e5e5e5;">

<p style="margin:0;font-size:15px;color:#444;">
Regards,<br>
<strong>Fundoo Notes Team</strong>
</p>

</td>
</tr>

<tr>
<td style="background:#f8f9fa;padding:18px;text-align:center;">

<p style="margin:0;font-size:13px;color:#888;">
© 2026 Fundoo Notes. All rights reserved.
</p>

</td>
</tr>

</table>

</td>
</tr>
</table>

</body>
</html>
""".formatted(link);

        sendEmail(
                toEmail,
                "Reset Your Fundoo Password",
                html
        );
    }

    @Override
    public void sendReminderEmail(String toEmail, String noteTitle) {

        String link = frontendUrl + "/signin";

        String html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Reminder - Fundoo Notes</title>
</head>

<body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,Helvetica,sans-serif;">

<table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
<tr>
<td align="center">

<table width="600" cellpadding="0" cellspacing="0"
style="background:#ffffff;border-radius:12px;overflow:hidden;
box-shadow:0 4px 15px rgba(0,0,0,0.15);">

<!-- Header -->
<tr>
<td style="background:#FBBC05;padding:25px;text-align:center;">

<h1 style="margin:0;color:white;font-size:30px;">
⏰ Fundoo Notes Reminder
</h1>

</td>
</tr>

<!-- Body -->
<tr>
<td style="padding:40px;">

<h2 style="margin-top:0;color:#333;text-align:center;">
Don't Forget Your Note!
</h2>

<p style="font-size:16px;color:#555;line-height:28px;">
Hello,
</p>

<p style="font-size:16px;color:#555;line-height:28px;">
This is a friendly reminder for your note on
<strong>Fundoo Notes</strong>.
</p>

<div style="
background:#f8f9fa;
border-left:4px solid #FBBC05;
padding:18px;
border-radius:6px;
margin:25px 0;
">

<p style="margin:0;font-size:15px;color:#555;">
📝 <strong>Note Title:</strong><br>
%s
</p>

</div>

<p style="font-size:16px;color:#555;line-height:28px;">
Click the button below to open Fundoo Notes and review your note.
</p>

<div style="text-align:center;margin:35px 0;">

<a href="%s"
style="
background:#FBBC05;
color:white;
text-decoration:none;
padding:15px 35px;
font-size:16px;
font-weight:bold;
border-radius:6px;
display:inline-block;">
View Note
</a>

</div>

<p style="font-size:15px;color:#666;line-height:24px;">
Stay organized by keeping track of your important notes and reminders.
</p>

<hr style="margin:35px 0;border:none;border-top:1px solid #e5e5e5;">

<p style="margin:0;font-size:15px;color:#444;">
Regards,<br>
<strong>Fundoo Notes Team</strong>
</p>

</td>
</tr>

<!-- Footer -->
<tr>
<td style="background:#f8f9fa;padding:18px;text-align:center;">

<p style="margin:0;font-size:13px;color:#888;">
© 2026 Fundoo Notes. All rights reserved.
</p>

</td>
</tr>

</table>

</td>
</tr>
</table>

</body>
</html>
""".formatted(noteTitle, link);

        sendEmail(
                toEmail,
                "Reminder: " + noteTitle + " - Fundoo Notes",
                html
        );
    }

    @Override
    public void sendCollaboratorEmail(String toEmail, String ownerEmail, String noteTitle) {

        String link = frontendUrl + "/signin";

        String html = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Note Shared With You</title>
</head>

<body style="margin:0;padding:0;background:#f3f4f6;font-family:Arial,Helvetica,sans-serif;">

<table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
<tr>
<td align="center">

<table width="600" cellpadding="0" cellspacing="0"
style="background:#ffffff;border-radius:12px;overflow:hidden;
box-shadow:0 4px 15px rgba(0,0,0,0.15);">

<!-- Header -->
<tr>
<td style="background:#4285F4;padding:25px;text-align:center;">

<h1 style="margin:0;color:white;font-size:30px;">
📝 Fundoo Notes
</h1>

</td>
</tr>

<!-- Body -->
<tr>
<td style="padding:40px;">

<h2 style="margin-top:0;color:#333;text-align:center;">
A Note Has Been Shared With You
</h2>

<p style="font-size:16px;color:#555;line-height:28px;">
Hello,
</p>

<p style="font-size:16px;color:#555;line-height:28px;">
<strong>%s</strong> has shared a note with you on
<strong>Fundoo Notes</strong>.
</p>

<div style="
background:#f8f9fa;
border-left:4px solid #4285F4;
padding:18px;
border-radius:6px;
margin:25px 0;
">

<p style="margin:0;font-size:15px;color:#555;">
📌 <strong>Note Title:</strong><br>
%s
</p>

</div>

<p style="font-size:16px;color:#555;line-height:28px;">
Click the button below to open Fundoo Notes and view the shared note.
</p>

<div style="text-align:center;margin:35px 0;">

<a href="%s"
style="
background:#4285F4;
color:white;
text-decoration:none;
padding:15px 35px;
font-size:16px;
font-weight:bold;
border-radius:6px;
display:inline-block;">
Open Fundoo Notes
</a>

</div>

<p style="font-size:15px;color:#666;line-height:24px;">
You can collaborate, edit, and manage notes together in real time.
</p>

<hr style="margin:35px 0;border:none;border-top:1px solid #e5e5e5;">

<p style="margin:0;font-size:15px;color:#444;">
Regards,<br>
<strong>Fundoo Notes Team</strong>
</p>

</td>
</tr>

<!-- Footer -->
<tr>
<td style="background:#f8f9fa;padding:18px;text-align:center;">

<p style="margin:0;font-size:13px;color:#888;">
© 2026 Fundoo Notes. All rights reserved.
</p>

</td>
</tr>

</table>

</td>
</tr>
</table>

</body>
</html>
""".formatted(ownerEmail, noteTitle, link);

        sendEmail(
                toEmail,
                "A Note Has Been Shared With You - Fundoo Notes",
                html
        );
    }

    private void sendEmail(String to,
                           String subject,
                           String body) {
        if (resendApiKey != null && !resendApiKey.trim().isEmpty()) {
            sendEmailViaResend(to, subject, body);
            return;
        }

        if (brevoApiKey != null && !brevoApiKey.trim().isEmpty()) {
            sendEmailViaBrevo(to, subject, body);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendEmailViaBrevo(String to, String subject, String htmlContent) {
        try {
            String escapedBody = htmlContent.replace("\\", "\\\\")
                                            .replace("\"", "\\\"")
                                            .replace("\n", "\\n")
                                            .replace("\r", "\\r");

            String jsonPayload = "{"
                    + "\"sender\":{\"name\":\"Fundoo Notes\",\"email\":\"" + fromEmail + "\"},"
                    + "\"to\":[{\"email\":\"" + to + "\"}],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"htmlContent\":\"" + escapedBody + "\""
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", brevoApiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException("Brevo API returned error status: " + response.statusCode() + " - " + response.body());
            }
            System.out.println("Email sent successfully via Brevo API!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Brevo API: " + e.getMessage(), e);
        }
    }

    private void sendEmailViaResend(String to, String subject, String htmlContent) {
        try {
            String escapedBody = htmlContent.replace("\\", "\\\\")
                                            .replace("\"", "\\\"")
                                            .replace("\n", "\\n")
                                            .replace("\r", "\\r");

            String sender = resendFrom;

            String jsonPayload = "{"
                    + "\"from\":\"" + sender + "\","
                    + "\"to\":[\"" + to + "\"],"
                    + "\"subject\":\"" + subject + "\","
                    + "\"html\":\"" + escapedBody + "\""
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException("Resend API error response: " + response.body());
            }
            System.out.println("Email sent successfully via Resend API!");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email via Resend API: " + e.getMessage(), e);
        }
    }
}
