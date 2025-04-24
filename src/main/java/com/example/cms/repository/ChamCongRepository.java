package com.example.cms.repository;

import com.example.cms.dao.ChamCong;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChamCongRepository extends MongoRepository<ChamCong, String> {

    List<ChamCong> findAllByAccountIdAndIsDeleteAndCreatedAtBetweenOrderByCreatedAtAsc(ObjectId accountId, Boolean isDelete, String createdAt, String createdAt2);

}
