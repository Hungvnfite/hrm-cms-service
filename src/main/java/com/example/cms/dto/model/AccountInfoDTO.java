package com.example.cms.dto.model;

import com.example.cms.dto.response.ChucVuResponse;
import com.example.cms.dto.response.PhongBanResponse;
import com.example.cms.dto.response.TrangThaiLamViecResponse;
import lombok.Data;

import java.util.List;

@Data
public class  AccountInfoDTO {

    private String accountId;
    private String maNv;
    private String fullName;
    private String userName;
    private ChucVuResponse chucVu;
    private PhongBanResponse phongBan;
    private String soCCCD;
    private Boolean gender;
    private String birth;
    private String phone;
    private String email;
    private String workStartDate;
    private TrangThaiLamViecResponse trangThai;
    private String office;
    private String jobPosition;
    private String diaChiHienTai;
    private String avatar;
    private List<String> files;
    private Integer typeAccount;
}
