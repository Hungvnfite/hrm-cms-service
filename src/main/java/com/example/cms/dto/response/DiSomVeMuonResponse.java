package com.example.cms.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

@Data
public class DiSomVeMuonResponse {

    private String id;
    private String accountId;
    private String department;
    private String fullName;
    private String statusLateEarly;
    private String statusResultLateEarly;
    private String dateLateEarly;
    private Integer timeLateEarly;
    private List<Map<String, String>> managers;
    private String notes;
    private String createdAt;
}