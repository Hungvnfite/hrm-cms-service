package com.example.cms.repository;

import com.example.cms.dao.PhongBan;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhongBanRepository extends MongoRepository<PhongBan, String> {
}
