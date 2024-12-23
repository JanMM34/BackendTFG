package com.ub.higiea.infrastructure.persistence.repositories;

import com.ub.higiea.infrastructure.persistence.entities.SensorEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface SensorEntityRepository extends ReactiveCrudRepository<SensorEntity, Long> {

    @Query("SELECT * FROM sensor WHERE assigned_to_route = false AND state != 'EMPTY' ORDER BY state DESC LIMIT :capacity")
    Flux<SensorEntity> findRelevantSensors(int capacity);

}