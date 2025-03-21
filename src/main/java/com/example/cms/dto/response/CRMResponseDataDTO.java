package com.example.cms.dto.response;

import lombok.Data;

@Data
public class CRMResponseDataDTO {

    private String message;
    private String code;
    private Object data;
}
