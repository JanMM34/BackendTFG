package com.ub.higiea.domain.repository;

import com.ub.higiea.domain.model.Truck;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TruckRepository {

    Mono<Truck> findById(UUID id);

    Flux<Truck> findAll();

    Mono<Truck> save(Truck truck);

    Mono<Truck> fetchOptimalTruck(int totalCapacity);

    Mono<Truck> fetchBiggestTruck();
}
