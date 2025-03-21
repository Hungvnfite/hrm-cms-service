package com.example.cms.dto.request;

import com.example.cms.dto.base.BaseRequest;
import lombok.Data;

@Data
public class ResetPasswordRequest extends BaseRequest {

    private String password;
}
