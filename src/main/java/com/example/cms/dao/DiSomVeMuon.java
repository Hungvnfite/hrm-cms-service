package com.example.cms.dao;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tbl_di_som_ve_muon")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiSomVeMuon {

    @Id
    @Field(name = "ID")
    @ApiModelProperty(value = "ID lần chấm công")
    private ObjectId id;

    @Indexed
    @Field(name = "ACCOUNT_ID")
    @ApiModelProperty(value = "ID tài khoản của nhân viên")
    private ObjectId accountId;

    @Indexed
    @Field(name = "STATUS_LATE_EARLY")
    @ApiModelProperty(value = "Trạng thái xin đi muộn, về sớm của ngày hôm đấy", example = "1: Đi muộn, 2: Về sớm")
    private String statusLateEarly;

    @Indexed
    @Field(name = "STATUS_RESULT_LATE_EARLY")
    @ApiModelProperty(value = "Trạng thái xin đi muộn, về sớm của ngày hôm đấy", example = "0: Đang chờ duyệt, 1: Chấp nhận, 2: Từ chối")
    private String statusResultLateEarly = "0";

    @Indexed
    @Field(name = "CREATED_AT")
    @ApiModelProperty(value = "Ngày giờ tạo bản ghi")
    private String createdAt;

    @Indexed
    @Field(name = "UPDATED_AT")
    @ApiModelProperty(value = "Ngày giờ chỉnh sửa bản ghi")
    private String updatedAt;

    @Indexed
    @Field(name = "IS_DELETE")
    @ApiModelProperty(value = "Trạng thái bản ghi")
    private Boolean isDelete = false;
}
