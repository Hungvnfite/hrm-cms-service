package com.example.cms.dto.response;

import lombok.Data;

@Data
public class ChamCongResponse {

    private String id;
    private String nameUser;
    private String checkinTime;
    private String checkoutTime;
    private String status;
    private String statusDetail;
    private String statusDetailOut;
    private String periodCheckin;
    private String periodCheckout;
    private String createdAt;
}
