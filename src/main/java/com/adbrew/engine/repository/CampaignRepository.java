package com.adbrew.engine.repository;

import com.adbrew.engine.domain.Campaign;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CampaignRepository extends MongoRepository<Campaign, String> {

    List<Campaign> findByAccountId(String accountId);
}
