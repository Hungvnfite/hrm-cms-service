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

@Document(collection = "tbl_account")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @Field(name = "ID")
    private ObjectId id;

    @Indexed
    @Field(name = "USERNAME")
    private String username;

    @Indexed
    @Field(name = "PASSWORD")
    private String password;

    @Indexed
    @Field(name = "TYPE_ACCOUNT")
    private Integer typeAccount;

    @Indexed
    @Field(name = "ROLE_IDS")
    private List<String> roleIds;

    @Indexed
    @Field(name = "IS_FIRST_PASSWORD")
    private Boolean isFirstPassword;

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
    @Field(name = "REFRESH_TOKEN")
    private String refreshToken;

}