package com.example.cms.dto.request;

import lombok.Data;

@Data
public class MailForgetPasswordRequest {
    private String fromEmail;
    private String toEmail;
    private String username;
    private String subject;
    private String newPassword;
}
