package com.ub.higiea.infrastructure.persistence.repositories;

import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.repository.SensorRepository;
import com.ub.higiea.infrastructure.persistence.entities.SensorEntity;
import com.ub.higiea.infrastructure.persistence.mapper.SensorMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class SensorRepositoryImpl implements SensorRepository {

    private final SensorEntityRepository sensorEntityRepository;

    public SensorRepositoryImpl(SensorEntityRepository sensorEntityRepository) {
        this.sensorEntityRepository = sensorEntityRepository;
    }

    @Override
    public Mono<Sensor> findById(Long id) {
        return sensorEntityRepository.findById(id)
                .map(SensorMapper::toDomain);
    }

    @Override
    public Flux<Sensor> findAll() {
        return sensorEntityRepository.findAll()
                .map(SensorMapper::toDomain);
    }

    @Override
    public Mono<Sensor> save(Sensor sensor) {
        SensorEntity entity = SensorMapper.toEntity(sensor);
        return sensorEntityRepository.save(entity)
                .map(SensorMapper::toDomain);
    }

}