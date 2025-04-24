package com.example.cms.dto.response;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NghiPhepResponse {

    private String id;
    private String accountId;
    private String department;
    private String fullName;
    private String startDate;
    private String endDate;
    private Integer isHalfStartDate;
    private Integer isHalfEndDate;
    private Integer leaveType;
    private Integer isHalfDay;
    private Integer status;
    private String reviewer;
    private List<Map<String, String>> managers;
    private String notes;
    private String createdAt;
}
