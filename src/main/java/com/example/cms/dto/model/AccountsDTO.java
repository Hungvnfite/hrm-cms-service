package com.example.cms.dto.model;

import lombok.Data;

import java.util.List;

@Data
public class AccountsDTO {

    List<AccountInfoDTO> accountInfoDTOS;
    long totalRecords;
}
