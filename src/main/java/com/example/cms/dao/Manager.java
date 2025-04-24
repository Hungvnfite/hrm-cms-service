package com.example.cms.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tbl_manager")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Manager {

    @Id
    @Field(name = "ID")
    private ObjectId id;

    @Indexed
    @Field(name = "NAME")
    private String name;

    @Indexed
    @Field(name = "DEPARTMENT")
    private String department;

    @Indexed
    @Field(name = "POSITION")
    private String position;

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
