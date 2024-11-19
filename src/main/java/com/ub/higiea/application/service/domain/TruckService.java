package com.ub.higiea.application.service.domain;

import com.ub.higiea.application.dtos.TruckDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
import com.ub.higiea.domain.model.Truck;
import com.ub.higiea.domain.repository.TruckRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TruckService {
    private final TruckRepository truckRepository;
    public TruckService(TruckRepository truckRepository) {
        this.truckRepository = truckRepository;
    }

    public Flux<TruckDTO> getAllTrucks() {
        return truckRepository.findAll()
                .map(TruckDTO::fromTruck);
    }

    public Mono<TruckDTO> getTruckById(Long id) {
        return truckRepository.findById(id)
                .switchIfEmpty(Mono.error(new TruckNotFoundException(id)))
                .map(TruckDTO::fromTruck);
    }

    public Mono<TruckDTO> createTruck() {
        Truck truck = Truck.create(null);
        return truckRepository.save(truck)
                .map(TruckDTO::fromTruck);
    }

}
