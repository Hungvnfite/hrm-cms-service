package com.example.cms.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tbl_trang_thai_lam_viec")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrangThaiLamViec {

    @Id
    @Field(name = "ID")
    private ObjectId id;

    @Indexed
    @Field(name = "TEN_TRANG_THAI")
    private String tenTrangThai;
}