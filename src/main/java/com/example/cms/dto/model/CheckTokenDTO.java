package com.example.cms.dto.model;

import lombok.Data;

@Data
public class CheckTokenDTO {

    private String accessToken;
    private String refreshToken;
}
