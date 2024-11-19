package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.service.domain.RouteService;
import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
import com.ub.higiea.application.requests.RouteCreateRequest;
import com.ub.higiea.domain.model.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@WebFluxTest(controllers = RouteController.class)
public class RouteControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RouteService routeService;

    @Test
    void getAllRoutes_ShouldReturnListOfRouteDTOs() {

        Sensor sensor1 = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(2L, Location.create(30.0, 60.0), ContainerState.EMPTY);
        Truck truck = Truck.create(1L);

        Route route1 = Route.create("1", truck, List.of(sensor1, sensor2), 100.0, 50L, List.of(Location.create(20.0, 10.0), Location.create(30.0, 60.0)));
        Route route2 = Route.create("2", truck, List.of(sensor2, sensor1), 500.0, 100L, List.of(Location.create(30.0, 60.0), Location.create(20.0, 10.0)));
        RouteDTO routeDTO1 = RouteDTO.fromRoute(route1);
        RouteDTO routeDTO2 = RouteDTO.fromRoute(route2);

        Mockito.when(routeService.getAllRoutes()).thenReturn(Flux.just(routeDTO1, routeDTO2));

        webTestClient.get()
                .uri("/routes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RouteDTO.class)
                .hasSize(2)
                .contains(routeDTO1, routeDTO2);
    }

    @Test
    void getRouteById_ShouldReturnRouteDTO_WhenRouteExists() {

        Sensor sensor1 = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(2L, Location.create(30.0, 60.0), ContainerState.EMPTY);
        Truck truck = Truck.create(1L);

        Route route = Route.create("1", truck, List.of(sensor1, sensor2), 100.0, 50L, List.of(Location.create(20.0, 10.0), Location.create(30.0, 60.0)));
        RouteDTO expectedRouteDTO = RouteDTO.fromRoute(route);

        String routeId = "1";
        Mockito.when(routeService.getRouteById(routeId)).thenReturn(Mono.just(expectedRouteDTO));

        webTestClient.get()
                .uri("/routes/{id}", routeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RouteDTO.class)
                .isEqualTo(expectedRouteDTO);

    }

    @Test
    void createRoute_ShouldReturnCreatedRouteDTO() {
        RouteCreateRequest routeCreateRequest = RouteCreateRequest.toRequest(
                1L,
                List.of(1L, 2L)
        );
        Sensor sensor1 = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(2L, Location.create(30.0, 60.0), ContainerState.EMPTY);
        Truck truck = Truck.create(1L);
        Route route = Route.create("1", truck, List.of(sensor1, sensor2), 100.0, 50L, List.of(Location.create(20.0, 10.0), Location.create(30.0, 60.0)));
        RouteDTO routeDTO = RouteDTO.fromRoute(route);

        Mockito.when(routeService.createRoute(Mockito.argThat(request ->
                request.getTruckId().equals(routeCreateRequest.getTruckId()) &&
                        request.getSensorIds().equals(routeCreateRequest.getSensorIds())
        ))).thenReturn(Mono.just(routeDTO));

        webTestClient.post()
                .uri("/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(routeCreateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(RouteDTO.class)
                .isEqualTo(routeDTO);
    }

    @Test
    void getRouteById_ShouldReturnNotFound_WhenRouteDoesNotExists() {
        String routeId = "1";
        Mockito.when(routeService.getRouteById(routeId)).thenReturn(Mono.error(new RouteNotFoundException(routeId)));

        webTestClient.get()
                .uri("/routes/{id}", routeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Route with id 1 not found");
    }

    @Test
    void createRoute_ShouldReturnBadRequest_WhenInputIsInvalid() {
        RouteCreateRequest invalidRequest = RouteCreateRequest.toRequest(null, List.of(1L, 2L));

        webTestClient.post()
                .uri("/routes")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

}