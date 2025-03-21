package com.example.cms.dto.model;

import lombok.Data;

import java.util.Date;

@Data
public class LoginDTO {

    private String accessToken;
    private String refreshToken;
    private Boolean isFirstPassword;
}
