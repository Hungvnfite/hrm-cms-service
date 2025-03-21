package com.example.cms.dto.request;

import com.example.cms.dto.base.BaseRequest;
import lombok.Data;

@Data
public class LoginRequest extends BaseRequest {

    private String username;
    private String password;
}
