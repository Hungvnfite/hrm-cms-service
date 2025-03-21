package com.example.cms.dto.request;

import lombok.Data;

import java.util.Date;

@Data
public class CreateAccountInfoCrmRequest {

    private String fullName;
    private String email;
    private Date birth;
    private String phone;
    private String address;
    private String position;
    private String workingStatus;
    private String department;
}
