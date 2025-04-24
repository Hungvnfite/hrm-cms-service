package com.example.cms.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class NghiPhepsResponse {

    private List<NghiPhepResponse> nghiPhepResponses;
    private Integer totalRecords;
}
