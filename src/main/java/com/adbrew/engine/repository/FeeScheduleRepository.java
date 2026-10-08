package com.adbrew.engine.repository;

import com.adbrew.engine.domain.FeeSchedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FeeScheduleRepository extends MongoRepository<FeeSchedule, String> {

    Optional<FeeSchedule> findByCategory(String category);
}
