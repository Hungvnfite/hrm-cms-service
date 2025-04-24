package com.example.cms.dao;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tbl_cham_cong")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel (description = "Thông tin chấm công của nhân viên")
public class ChamCong {
    @Id
    @Field(name = "ID")
    @ApiModelProperty(value = "ID lần chấm công")
    private ObjectId id;

    @Indexed
    @Field(name = "ACCOUNT_ID")
    @ApiModelProperty(value = "ID tài khoản của nhân viên")
    private ObjectId accountId;

    @Indexed
    @Field(name = "CHECKIN_TIME")
    @ApiModelProperty(value = "Thời gian chấm công vào làm")
    private String checkinTime = null;

    @Indexed
    @Field(name = "CHECKOUT_TIME")
    @ApiModelProperty(value = "Thời gian chấm công ra về")
    private String checkoutTime = null;

    @Indexed
    @Field(name = "STATUS")
    @ApiModelProperty(value = "Trạng thái chấm công của ngày hôm đấy", example = "0: Hợp lệ, 1: Quên chấm công đến, 2:Quên chấm công về, 3:Thất bại")
    private String status;

    @Indexed
    @Field(name = "STATUS_DETAIL")
    @ApiModelProperty(value = "Trạng thái chấm công của ngày hôm đấy", example = "0: Hợp lệ, 1: Đi muộn, 2: Thất bại")
    private String statusDetail = null;

    @Indexed
    @Field(name = "STATUS_DETAIL_OUT")
    @ApiModelProperty(value = "Trạng thái chấm công của ngày hôm đấy", example = "0: Hợp lệ, 1: Về sớm,2: Thất bại")
    private String statusDetailOut = null;

    @Indexed
    @Field(name = "PERIOD_CHECKIN")
    @ApiModelProperty(value = "Thời gian đi muộn", example = "30")
    private String periodIn = "0";

    @Indexed
    @Field(name = "PERIOD_CHECKOUT")
    @ApiModelProperty(value = "Thời gian về sớm", example = "30")
    private String periodOut = "0";

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
