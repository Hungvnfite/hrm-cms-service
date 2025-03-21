package com.example.cms.repository;

import com.example.cms.dao.HoSo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HoSoRepository extends MongoRepository<HoSo, String> {

    HoSo findByIdAndIsDelete(ObjectId id, Boolean isDelete);
}
