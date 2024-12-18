package com.ub.higiea.application.services.domain;

import com.ub.higiea.application.dtos.TruckDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
import com.ub.higiea.application.requests.TruckCreateRequest;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Truck;
import com.ub.higiea.domain.repository.TruckRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TruckServiceTest {

    @Mock
    private TruckRepository truckRepository;

    @InjectMocks
    private TruckService truckService;

    @Test
    void getAllTrucks_ShouldReturnAllTruckDTOs_WhenRepositoryNotEmpty() {
        Truck truck1 = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));
        Truck truck2 = Truck.create(UUID.randomUUID(),"2", 20, Location.create(30.0, 40.0));

        when(truckRepository.findAll()).thenReturn(Flux.just(truck1, truck2));

        StepVerifier.create(truckService.getAllTrucks())
                .expectNextMatches(dto -> dto.equals(TruckDTO.fromTruck(truck1)))
                .expectNextMatches(dto -> dto.equals(TruckDTO.fromTruck(truck2)))
                .verifyComplete();

        verify(truckRepository, times(1)).findAll();
    }

    @Test
    void getAllTrucks_ShouldReturnEmptyFlux_WhenRepositoryIsEmpty() {
        when(truckRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(truckService.getAllTrucks())
                .verifyComplete();

        verify(truckRepository, times(1)).findAll();
    }

    @Test
    void getTruckById_ShouldReturnTruckDTO_WhenTruckFound() {
        Truck truck = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));

        when(truckRepository.findById(truck.getId())).thenReturn(Mono.just(truck));

        StepVerifier.create(truckService.getTruckById(truck.getId()))
                .expectNextMatches(dto -> dto.equals(TruckDTO.fromTruck(truck)))
                .verifyComplete();

        verify(truckRepository, times(1)).findById(truck.getId());
    }

    @Test
    void getTruckById_ShouldReturnError_WhenTruckNotFound() {
        UUID truckId = UUID.randomUUID();
        when(truckRepository.findById(truckId)).thenReturn(Mono.empty());

        StepVerifier.create(truckService.getTruckById(truckId))
                .expectErrorMatches(throwable -> throwable instanceof TruckNotFoundException)
                .verify();

        verify(truckRepository, times(1)).findById(truckId);
    }

    @Test
    void createTruck_ShouldReturnCreatedTruckDTO_WhenValidInput() {
        TruckCreateRequest request = TruckCreateRequest
                .toRequest("1",10.0, 20.0, 15);

        Truck truck = Truck.create(UUID.randomUUID(),request.getPlate(),
                request.getMaxLoadCapacity(),
                Location.create(request.getLatitude(), request.getLongitude())
        );

        when(truckRepository.save(any(Truck.class))).thenReturn(Mono.just(truck));

        StepVerifier.create(truckService.createTruck(request))
                .expectNextMatches(dto -> dto.equals(TruckDTO.fromTruck(truck)))
                .verifyComplete();

        ArgumentCaptor<Truck> truckCaptor = ArgumentCaptor.forClass(Truck.class);
        verify(truckRepository).save(truckCaptor.capture());
        Truck savedTruck = truckCaptor.getValue();

        assertEquals(request.getLatitude(), savedTruck.getDepotLocation().getLatitude());
        assertEquals(request.getLongitude(), savedTruck.getDepotLocation().getLongitude());
        assertEquals(request.getMaxLoadCapacity(), savedTruck.getMaxLoadCapacity());
    }

}
