package com.example.cms.repository;

import com.example.cms.dao.ChucVu;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChucVuRepository extends MongoRepository<ChucVu, String> {
}
