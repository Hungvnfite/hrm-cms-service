package com.example.cms.repository;

import com.example.cms.dao.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Account findTopByUsernameAndIsDelete(String username, Boolean isDelete);

    Account findByRefreshToken(String refreshToken);

    List<Account> findByUsernameStartingWith(String usernamePrefix);

    Page<Account> findAllByIsDeleteOrderByCreatedAt(Boolean isDelete, Pageable pageable);

    List<Account> findByIsForgottenTrue();
}
