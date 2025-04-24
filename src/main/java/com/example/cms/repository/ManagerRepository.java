package com.example.cms.repository;

import com.example.cms.dao.Manager;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagerRepository extends MongoRepository<Manager, String> {

    List<Manager> findAllByIsDelete(Boolean isDelete);
}
