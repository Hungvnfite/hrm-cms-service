package com.example.cms.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionData implements Serializable {

    private String sessionId;
    private String accountId;
    private String refreshToken;
    private Integer typeAccount;
}
