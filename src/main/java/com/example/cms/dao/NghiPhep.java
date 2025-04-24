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

import java.util.List;

@Document(collection = "tbl_nghi_phep")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NghiPhep {

    @Id
    @Field(name = "ID")
    private ObjectId id;

    @Indexed
    @Field(name = "ACCOUNT_ID")
    private ObjectId accountId;

    @Indexed
    @Field(name = "START_DATE")
    private String startDate;

    @Indexed
    @Field(name = "END_DATE")
    private String endDate;

    @Indexed
    @Field(name = "IS_HALF_START_DATE")
    @ApiModelProperty(value = "Thời gian nghỉ ngày bắt đầu", example = "0: Nửa ngày, 1: Một ngày")
    private Integer isHalfStartDate;

    @Indexed
    @Field(name = "IS_HALF_END_DATE")
    @ApiModelProperty(value = "Thời gian nghỉ ngày kết thúc", example = "0: Nửa ngày, 1: Một ngày")
    private Integer isHalfEndDate;

    @Indexed
    @Field(name = "LEAVE_TYPE")
    @ApiModelProperty(value = "Trạng thái nghỉ phép", example = "0: Có lương, 1: Không lương")
    private Integer leaveType;

    @Indexed
    @Field(name = "IS_HALF_DAY")
    @ApiModelProperty(value = "Thời gian nghỉ phép", example = "0: Nửa ngày, 1: Một ngày, 2: Dài ngày")
    private Integer isHalfDay;

    @Indexed
    @Field(name = "STATUS")
    @ApiModelProperty(value = "Trạng thái", example = "0: Chưa duyệt, 1: Đã duyệt, 2: Từ chối")
    private Integer status;

    @Indexed
    @Field(name = "REVIEWER")
    private ObjectId reviewer;

    @Indexed
    @Field(name = "MANAGERS")
    private List<ObjectId> managers;

    @Indexed
    @Field(name = "NOTES")
    private String notes;

    @Indexed
    @Field(name = "CREATED_AT")
    private String createdAt;

    @Indexed
    @Field(name = "UPDATED_AT")
    private String updatedAt;

    @Indexed
    @Field(name = "IS_DELETE")
    private Boolean isDelete = false;
}
