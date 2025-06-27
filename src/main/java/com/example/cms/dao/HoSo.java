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

@Document(collection = "tbl_ho_so")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HoSo {

    @Id
    @Field(name = "ID")
    private ObjectId id;

    @Indexed
    @Field(name = "NAME")
    private String name;

    @Indexed
    @Field(name = "IMAGE_ID")
    private String imageId;

    @Indexed
    @Field(name = "PATH")
    private String path;

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
    @Field(name = "CAPACITY")
    private String capacity;

    @Indexed
    @Field(name = "TYPE")
    private String type;
}
