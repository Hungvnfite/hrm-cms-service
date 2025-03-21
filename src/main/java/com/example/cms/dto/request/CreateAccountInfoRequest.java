package com.example.cms.dto.request;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.lang.Nullable;

import java.util.Date;
import java.util.List;

@Data
public class CreateAccountInfoRequest {

    private Boolean isFirstPassword;
    private Integer typeAccount;
    private String fullName;
    private String diaChiHienTai;
    private Date birth;
    private ObjectId idPhongBan;
    private ObjectId idChucVu;
    private ObjectId idTrangThai;
    private String soCCCD;
    private Boolean gender;
    private Date workStartDate;
    private String phone;
    private String email;
    private List<String> filesOld;

}
