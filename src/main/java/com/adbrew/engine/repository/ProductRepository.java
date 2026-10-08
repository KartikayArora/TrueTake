package com.adbrew.engine.repository;

import com.adbrew.engine.domain.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    List<Product> findByAccountId(String accountId);
}
