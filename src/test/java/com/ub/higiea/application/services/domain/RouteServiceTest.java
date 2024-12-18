package com.ub.higiea.application.services.domain;

import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.dtos.RouteSummaryDTO;
import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
import com.ub.higiea.application.strategies.RouteCalculator;
import com.ub.higiea.application.strategies.RouteCalculationResult;
import com.ub.higiea.domain.model.*;
import com.ub.higiea.domain.repository.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class RouteServiceTest {

    @Mock
    private RouteCalculator routeCalculator;

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    @Test
    void getAllRoutes_ShouldReturnAllRouteDTOs_WhenRepositoryNotEmpty() {
        Truck truck = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));
        Sensor sensor1 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(UUID.randomUUID(), Location.create(30.0, 20.0), ContainerState.FULL);
        Route route1 = Route.create("1", truck, List.of(sensor1, sensor2), 100.0, 50L,
                List.of(Location.create(20.0, 10.0), Location.create(30.0, 20.0)));
        Route route2 = Route.create("2", truck, List.of(sensor2, sensor1), 200.0, 70L,
                List.of(Location.create(30.0, 20.0), Location.create(20.0, 10.0)));

        when(routeRepository.findAll()).thenReturn(Flux.just(route1, route2));

        StepVerifier.create(routeService.getAllRoutes())
                .expectNext(RouteSummaryDTO.fromRoute(route1))
                .expectNext(RouteSummaryDTO.fromRoute(route2))
                .verifyComplete();

        verify(routeRepository, times(1)).findAll();
    }

    @Test
    void getAllRoutes_ShouldReturnEmptyFlux_WhenRepositoryIsEmpty() {
        when(routeRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(routeService.getAllRoutes())
                .verifyComplete();

        verify(routeRepository, times(1)).findAll();
    }

    @Test
    void getRouteById_ShouldReturnRouteDTO_WhenRouteFound() {
        String routeId = "1";
        Truck truck = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));
        Sensor sensor1 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);
        Route route = Route.create(routeId, truck, List.of(sensor1), 100.0, 50L, List.of(Location.create(20.0, 10.0)));

        when(routeRepository.findById(routeId)).thenReturn(Mono.just(route));

        StepVerifier.create(routeService.getRouteById(routeId))
                .expectNext(RouteDTO.fromRoute(route))
                .verifyComplete();

        verify(routeRepository, times(1)).findById(routeId);
    }

    @Test
    void getRouteById_ShouldReturnError_WhenRouteNotFound() {
        String routeId = "1";
        when(routeRepository.findById(routeId)).thenReturn(Mono.empty());

        StepVerifier.create(routeService.getRouteById(routeId))
                .expectErrorMatches(throwable -> throwable instanceof RouteNotFoundException)
                .verify();

        verify(routeRepository, times(1)).findById(routeId);
    }

    @Test
    void calculateAndSaveRoute_ShouldReturnSavedRoute_WhenValidInput() {
        Truck truck = Truck.create(UUID.randomUUID(),"1", 10, Location.create(10.0, 20.0));
        Sensor sensor1 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(UUID.randomUUID(), Location.create(30.0, 20.0), ContainerState.FULL);
        List<Sensor> sensors = List.of(sensor1, sensor2);

        RouteCalculationResult calculationResult = new RouteCalculationResult(
                sensors,
                30.0,
                45L,
                List.of(Location.create(10.0, 20.0), Location.create(20.0, 10.0), Location.create(30.0, 20.0))
        );

        Route expectedRoute = Route.create(null, truck, sensors, 30.0, 45L, calculationResult.routeGeometry());

        when(routeCalculator.calculateRoute(truck.getDepotLocation(), sensors)).thenReturn(Mono.just(calculationResult));
        when(routeRepository.save(any(Route.class))).thenReturn(Mono.just(expectedRoute));

        StepVerifier.create(routeService.calculateAndSaveRoute(truck, sensors))
                .expectNextMatches(route -> route.getTotalDistance().equals(30.0) &&
                        route.getEstimatedTimeInSeconds().equals(45L) &&
                        route.getSensors().equals(sensors))
                .verifyComplete();

        ArgumentCaptor<Route> routeCaptor = ArgumentCaptor.forClass(Route.class);
        verify(routeRepository).save(routeCaptor.capture());
        Route savedRoute = routeCaptor.getValue();

        assertEquals(truck, savedRoute.getTruck());
        assertEquals(sensors, savedRoute.getSensors());
        assertEquals(30.0, savedRoute.getTotalDistance());
        assertEquals(45L, savedRoute.getEstimatedTimeInSeconds());
        assertEquals(calculationResult.routeGeometry(), savedRoute.getRouteGeometry());
    }

}
