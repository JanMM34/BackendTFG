package com.ub.higiea.application.service;

import com.ub.higiea.application.dtos.TruckDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
import com.ub.higiea.application.service.domain.TruckService;
import com.ub.higiea.domain.model.Truck;
import com.ub.higiea.domain.repository.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class TruckServiceTest {

    private TruckService truckService;
    private TruckRepository truckRepository;

    @BeforeEach
    public void setUp() {
        truckRepository = Mockito.mock(TruckRepository.class);
        truckService = new TruckService(truckRepository);
    }

    @Test
    void getAllTrucks_ShouldReturnAllTrucksDTOs_WhenRepositoryNotEmpty() {
        Truck truck1 = Truck.create(1L);
        Truck truck2 = Truck.create(2L);

        TruckDTO expectedTruck1DTO = TruckDTO.fromTruck(truck1);
        TruckDTO expectedTruck2DTO = TruckDTO.fromTruck(truck2);

        Mockito.when(truckRepository.findAll()).thenReturn(Flux.just(truck1, truck2));

        StepVerifier.create(truckService.getAllTrucks())
                .expectNextMatches(dto -> dto.equals(expectedTruck1DTO))
                .expectNextMatches(dto -> dto.equals(expectedTruck2DTO))
                .verifyComplete();
    }

    @Test
    void getAllTrucks_shouldReturnEmptyFlux_whenRepositoryIsEmpty() {
        Mockito.when(truckRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(truckService.getAllTrucks())
                .verifyComplete();
    }

    @Test
    void getTruckById_shouldReturnTruckDTO_WhenTruckFound() {
        Truck truck = Truck.create(1L);
        TruckDTO expectedTruckDTO = TruckDTO.fromTruck(truck);

        Mockito.when(truckRepository.findById(truck.getId())).thenReturn(Mono.just(truck));

        StepVerifier.create(truckService.getTruckById(truck.getId()))
                .expectNextMatches(dto -> dto.equals(expectedTruckDTO))
                .verifyComplete();
    }

    @Test
    void getTruckById_shouldReturnError_whenTruckNotFound() {
        Long truckId = 999L;
        Mockito.when(truckRepository.findById(truckId)).thenReturn(Mono.empty());

        StepVerifier.create(truckService.getTruckById(truckId))
                .expectError(TruckNotFoundException.class)
                .verify();
    }

    @Test
    void createTruck_shouldReturnCreatedTruckDTO_WhenTruckCreated() {
        Truck truck = Truck.create(1L);
        TruckDTO expectedTruckDTO = TruckDTO.fromTruck(truck);
        Mockito.when(truckRepository.save(Mockito.any(Truck.class))).thenReturn(Mono.just(truck));

        StepVerifier.create(truckService.createTruck())
                .expectNextMatches(dto -> dto.equals(expectedTruckDTO))
                .verifyComplete();
    }

}
