package com.example.cms.repository;

import com.example.cms.dao.NghiPhep;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NghiPhepRepository extends MongoRepository<NghiPhep, String> {

    List<NghiPhep> findAllByAccountIdAndStatusAndIsDelete(ObjectId accountId, Integer status, Boolean isDelete);

    List<NghiPhep> findAllByAccountIdAndStatusNotAndIsDelete(ObjectId accountId, Integer status, Boolean isDelete);

    Page<NghiPhep> findAllByStatusAndIsDelete(Integer status, Boolean isDelete, Pageable pageable);

    Page<NghiPhep> findAllByStatusNotAndIsDelete(Integer status, Boolean isDelete, Pageable pageable);
}
