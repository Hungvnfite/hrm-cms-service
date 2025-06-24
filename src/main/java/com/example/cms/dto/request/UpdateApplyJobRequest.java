package com.example.cms.dto.request;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UpdateApplyJobRequest {

    //base
    private String applyJobId;
    private Integer status;

    //send mail
    private String interviewStatus;
    private Timestamp interviewDate;

    //create Account
    private String username;
    private String password;
}