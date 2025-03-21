package com.example.cms.repository;

import com.example.cms.dao.TrangThaiLamViec;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrangThaiLamViecRepository extends MongoRepository<TrangThaiLamViec, String> {
}
