package com.ub.higiea.infrastructure.persistence.repositories;

import com.ub.higiea.domain.model.Truck;
import com.ub.higiea.domain.repository.TruckRepository;
import com.ub.higiea.infrastructure.persistence.entities.TruckEntity;
import com.ub.higiea.infrastructure.persistence.mapper.TruckMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class TruckRepositoryImpl implements TruckRepository {

    private final TruckEntityRepository truckEntityRepository;

    public TruckRepositoryImpl(TruckEntityRepository truckEntityRepository) {
        this.truckEntityRepository = truckEntityRepository;
    }

    @Override
    public Mono<Truck> findById(UUID id) {
        return truckEntityRepository.findById(id)
                .map(TruckMapper::toDomain);
    }

    @Override
    public Flux<Truck> findAll() {
        return truckEntityRepository.findAll()
                .map(TruckMapper::toDomain);
    }

    @Override
    public Mono<Truck> save(Truck truck) {
        TruckEntity entity = TruckMapper.toEntity(truck);
        return truckEntityRepository.save(entity)
                .map(TruckMapper::toDomain);
    }

    @Override
    public Mono<Truck> fetchOptimalTruck(int totalCapacity) {
        return truckEntityRepository.findOptimalTruck(totalCapacity)
                .map(TruckMapper::toDomain);
    }

    @Override
    public Mono<Truck> fetchBiggestTruck() {
        return truckEntityRepository.findBiggestTruck()
                .map(TruckMapper::toDomain);
    }

}