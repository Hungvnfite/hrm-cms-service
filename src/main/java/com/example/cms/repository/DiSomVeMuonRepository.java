package com.example.cms.repository;

import com.example.cms.dao.DiSomVeMuon;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiSomVeMuonRepository extends MongoRepository<DiSomVeMuon, String> {

    List<DiSomVeMuon> findAllByAccountIdAndStatusResultLateEarlyAndIsDeleteAndCreatedAtBetweenOrderByCreatedAtAsc(ObjectId accountId, String statusResultLateEarly, Boolean isDelete, String createdAt, String createdAt2);

    DiSomVeMuon findByCreatedAtAndAccountId(String createdAt, ObjectId accountId);
}
