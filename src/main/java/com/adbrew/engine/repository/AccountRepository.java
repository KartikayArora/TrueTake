package com.adbrew.engine.repository;

import com.adbrew.engine.domain.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {
}
