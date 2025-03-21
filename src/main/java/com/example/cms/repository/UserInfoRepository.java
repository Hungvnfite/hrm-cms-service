package com.example.cms.repository;

import com.example.cms.dao.UserInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfo, String> {

    UserInfo findTopByOrderByMaNvDesc();

    UserInfo findByUsernameAndIsDelete(String maNv, Boolean isDelete);

    List<UserInfo> findByUsernameInAndIsDelete(List<String> maNv, Boolean isDelete);
}
