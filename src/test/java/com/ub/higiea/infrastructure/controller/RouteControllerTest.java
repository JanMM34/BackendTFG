package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.dtos.RouteSummaryDTO;
import com.ub.higiea.application.services.MessageService;
import com.ub.higiea.application.services.domain.RouteService;
import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
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
import java.util.UUID;

@WebFluxTest(controllers = RouteController.class)
public class RouteControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RouteService routeService;

    @MockBean
    private MessageService messageService;

    @Test
    void getAllRoutes_ShouldReturnCombinedGeoJSON_WhenRoutesExist() {

        Sensor sensor1 = Sensor.create(
                UUID.randomUUID(),
                Location.create(20.0, 10.0),
                ContainerState.EMPTY);

        Sensor sensor2 = Sensor.create(
                UUID.randomUUID(),
                Location.create(30.0, 60.0),
                ContainerState.FULL
        );

        Truck truck = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));

        Route route1 = Route.create("1", truck, List.of(sensor1, sensor2), 100.0, 50L,
                List.of(Location.create(20.0, 10.0), Location.create(30.0, 60.0))
        );
        Route route2 = Route.create("2", truck, List.of(sensor2, sensor1), 500.0, 100L,
                List.of(Location.create(30.0, 60.0), Location.create(20.0, 10.0))
        );

        RouteSummaryDTO summaryDTO1 = RouteSummaryDTO.fromRoute(route1);
        RouteSummaryDTO summaryDTO2 = RouteSummaryDTO.fromRoute(route2);


        Mockito.when(routeService.getAllRoutes()).thenReturn(Flux.just(summaryDTO1, summaryDTO2));

        webTestClient.get()
                .uri("/routes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RouteSummaryDTO.class)
                .contains(summaryDTO1, summaryDTO2);
    }

    @Test
    void getAllRoutes_ShouldReturnEmptyGeoJSON_WhenNoRoutesExist() {
        Mockito.when(routeService.getAllRoutes()).thenReturn(Flux.empty());
        webTestClient.get()
                .uri("/routes")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(RouteSummaryDTO.class)
                .hasSize(0);
    }

    @Test
    void getRouteById_ShouldReturnGeoJSON_WhenRouteExists() {

        Sensor sensor1 = Sensor.create(
                UUID.randomUUID(),
                Location.create(20.0, 10.0),
                ContainerState.EMPTY
        );

        Sensor sensor2 = Sensor.create(
                UUID.randomUUID(),
                Location.create(30.0, 60.0),
                ContainerState.FULL
        );

        Truck truck = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));

        Route route = Route.create("1", truck, List.of(sensor1, sensor2), 100.0, 50L,
                List.of(Location.create(20.0, 10.0), Location.create(30.0, 60.0))
        );

        RouteDTO expectedRouteDTO = RouteDTO.fromRoute(route);

        String routeId = "1";
        Mockito.when(routeService.getRouteById(routeId)).thenReturn(Mono.just(expectedRouteDTO));

        webTestClient.get()
                .uri("/routes/{id}", routeId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .isEqualTo(expectedRouteDTO.toGeoJSON());
    }

    @Test
    void getRouteById_ShouldReturnNotFound_WhenRouteDoesNotExist() {
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

}