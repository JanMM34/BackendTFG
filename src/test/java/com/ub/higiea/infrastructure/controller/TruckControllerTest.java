package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.service.domain.TruckService;
import com.ub.higiea.application.dtos.TruckDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
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

@WebFluxTest(controllers = TruckController.class)
public class TruckControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private TruckService truckService;

    @Test
    void getAllTrucks_ShouldReturnListOfTruckDTOs() {

        Truck truck1 = Truck.create(1L);
        Truck truck2 = Truck.create(2L);

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
    void gettruckById_ShouldReturntruckDTO_WhentruckExists() {

        Truck truck = Truck.create(1L);

        TruckDTO expectedtruckDTO = TruckDTO.fromTruck(truck);

        Mockito.when(truckService.getTruckById(1L)).thenReturn(Mono.just(expectedtruckDTO));

        webTestClient.get()
                .uri("/trucks/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TruckDTO.class)
                .isEqualTo(expectedtruckDTO);
    }

    @Test
    void createTruck_ShouldReturnCreatedTruckDTO() {

        Truck truck = Truck.create(1L);
        TruckDTO truckDTO = TruckDTO.fromTruck(truck);

        Mockito.when(truckService.createTruck()).thenReturn(Mono.just(truckDTO));

        webTestClient.post()
                .uri("/trucks")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(TruckDTO.class)
                .isEqualTo(truckDTO);
    }

    @Test
    void gettruckById_ShouldReturnNotFound_WhentruckNotFound() {
        Long truckId = 1L;
        Mockito.when(truckService.getTruckById(truckId))
                .thenReturn(Mono.error(new TruckNotFoundException(truckId)));

        webTestClient.get()
                .uri("/trucks/{id}", truckId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Truck with id 1 not found");
    }

}
