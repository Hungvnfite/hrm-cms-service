package com.example.cms.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tbl_role")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @Field(name = "ID")
    private String id;

    @Field(name = "MO_TA")
    private String moTa;
}