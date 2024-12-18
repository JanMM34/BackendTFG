package com.ub.higiea.infrastructure.persistence.repositories;

import com.ub.higiea.infrastructure.persistence.entities.SensorEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SensorEntityRepository extends ReactiveCrudRepository<SensorEntity, UUID> {

    @Query("SELECT * FROM sensor " +
            "WHERE (assigned_route IS NULL OR assigned_route = '')" +
            "AND state IN ('FULL', 'HALF') " +
            "ORDER BY CASE WHEN state = 'FULL' THEN 0 ELSE 1 END ASC")
    Flux<SensorEntity> findUnassignedFullOrHalfSensorsSortedByPriority();

}