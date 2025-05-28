package com.example.cms.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class DiSomVeMuonsResponse {
    private List<DiSomVeMuonResponse> resultData;
    private Integer totalRecords;
}
