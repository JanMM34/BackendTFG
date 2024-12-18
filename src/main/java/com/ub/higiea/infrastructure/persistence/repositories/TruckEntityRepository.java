package com.ub.higiea.infrastructure.persistence.repositories;


import com.ub.higiea.infrastructure.persistence.entities.TruckEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TruckEntityRepository extends ReactiveCrudRepository<TruckEntity, UUID> {

    @Query("SELECT * FROM truck " +
            "WHERE max_load_capacity >= :requiredCapacity " +
            "AND (route IS NULL OR route = '') " +
            "ORDER BY max_load_capacity ASC " +
            "LIMIT 1")
    Mono<TruckEntity> findOptimalTruck(int requiredCapacity);


    @Query("SELECT * FROM truck " +
            "WHERE (route IS NULL OR route = '') " +
            "ORDER BY max_load_capacity DESC " +
            "LIMIT 1")
    Mono<TruckEntity> findBiggestTruck();
}