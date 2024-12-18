package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.requests.TruckCreateRequest;
import com.ub.higiea.application.services.domain.TruckService;
import com.ub.higiea.application.dtos.TruckDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Truck;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@WebFluxTest(controllers = TruckController.class)
public class TruckControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TruckService truckService;

    @Test
    void getAllTrucks_ShouldReturnListOfTruckDTOs() {
        Truck truck1 = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));
        Truck truck2 = Truck.create(UUID.randomUUID(),"2", 20, Location.create(30.0, 40.0));

        TruckDTO truckDTO1 = TruckDTO.fromTruck(truck1);
        TruckDTO truckDTO2 = TruckDTO.fromTruck(truck2);

        Mockito.when(truckService.getAllTrucks()).thenReturn(Flux.just(truckDTO1, truckDTO2));

        webTestClient.get().uri("/trucks")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TruckDTO.class)
                .hasSize(2)
                .contains(truckDTO1, truckDTO2);
    }

    @Test
    void createTruck_ShouldReturnCreatedTruckDTO() {
        TruckCreateRequest truckCreateRequest = TruckCreateRequest.toRequest("1",10.0, 20.0, 15);
        Truck truck = Truck.create(UUID.randomUUID(),truckCreateRequest.getPlate(), truckCreateRequest.getMaxLoadCapacity(),
                Location.create(truckCreateRequest.getLatitude(), truckCreateRequest.getLongitude()));
        TruckDTO truckDTO = TruckDTO.fromTruck(truck);

        Mockito.when(truckService.createTruck(Mockito.any(TruckCreateRequest.class))).thenReturn(Mono.just(truckDTO));

        webTestClient.post()
                .uri("/trucks")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(truckCreateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TruckDTO.class)
                .isEqualTo(truckDTO);
    }

    @Test
    void getTruckById_ShouldReturnNotFound_WhenTruckNotFound() {
        UUID truckId = UUID.randomUUID();
        Mockito.when(truckService.getTruckById(truckId))
                .thenReturn(Mono.error(new TruckNotFoundException(truckId)));

        webTestClient.get()
                .uri("/trucks/{id}", truckId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

}
