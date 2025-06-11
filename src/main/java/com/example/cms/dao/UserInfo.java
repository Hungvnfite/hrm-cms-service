package com.example.cms.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "tbl_user_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    @Id
    @Field(name = "ID")
    private ObjectId id;

    @Indexed
    @Field(name = "USERNAME")
    private String username;

    @Indexed
    @Field(name = "MA_NV")
    private String maNv;

    @Indexed
    @Field(name = "FULL_NAME")
    private String fullName;

    @Indexed
    @Field(name = "DIA_CHI_HIEN_TAI")
    private String diaChiHienTai;

    @Indexed
    @Field(name = "BIRTH")
    private String birth;

    @Indexed
    @Field(name = "AVATAR_URL")
    private String avatarUrl;

    @Indexed
    @Field(name = "AVATAR_ID")
    private String avatarId;

    @Indexed
    @Field(name = "ID_PHONG_BAN")
    private ObjectId idPhongBan;

    @Indexed
    @Field(name = "ID_CHUC_VU")
    private ObjectId idChucVu;

    @Indexed
    @Field(name = "ID_TRANG_THAI")
    private ObjectId idTrangThai;

    @Indexed
    @Field(name = "ID_HO_SO")
    private List<ObjectId> idHoSo;

    @Indexed
    @Field(name = "SO_CCCD")
    private String soCCCD;

    @Indexed
    @Field(name = "CREATED_AT")
    private String createdAt;

    @Indexed
    @Field(name = "UPDATED_AT")
    private String updatedAt;

    @Indexed
    @Field(name = "IS_DELETE")
    private Boolean isDelete = false;

    @Indexed
    @Field(name = "WORK_START_DATE")
    private String workStartDate;

    @Indexed
    @Field(name = "GENDER")
    private Boolean gender;

    @Indexed
    @Field(name = "PHONE")
    private String phone;

    @Indexed
    @Field(name = "EMAIL")
    private String email;

    @Indexed
    @Field(name = "NUMBER_OF_DAYS_OFF_REMAINING")
    private Double numberOfDaysOffRemaining;
}