package com.ub.higiea.application.service;

import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.requests.RouteCreateRequest;
import com.ub.higiea.application.service.domain.RouteService;
import com.ub.higiea.application.service.external.RouteCalculatorService;
import com.ub.higiea.application.responses.RouteCalculationResult;
import com.ub.higiea.domain.model.*;
import com.ub.higiea.domain.repository.RouteRepository;
import com.ub.higiea.domain.repository.SensorRepository;
import com.ub.higiea.domain.repository.TruckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

public class RouteServiceTest {

    private RouteService routeService;
    private RouteCalculatorService routeCalculatorService;
    private RouteRepository routeRepository;
    private TruckRepository truckRepository;
    private SensorRepository sensorRepository;

    @BeforeEach
    public void setUp() {
        routeCalculatorService = Mockito.mock(RouteCalculatorService.class);
        routeRepository = Mockito.mock(RouteRepository.class);
        truckRepository = Mockito.mock(TruckRepository.class);
        sensorRepository = Mockito.mock(SensorRepository.class);
        routeService = new RouteService(routeCalculatorService, routeRepository, truckRepository, sensorRepository);
    }

    @Test
    void createRoute_shouldReturnRouteDTO_WhenValidInput() {

        Long truckId = 1L;
        List<Long> sensorIds = List.of(1L, 2L);

        RouteCreateRequest request = RouteCreateRequest.toRequest(truckId, sensorIds);

        Truck truck = Truck.create(truckId);
        Sensor sensor1 = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(2L, Location.create(30.0, 20.0), ContainerState.FULL);
        List<Sensor> sensors = List.of(sensor1, sensor2);
        List<Sensor> orderedSensors = List.of(sensor1, sensor2);
        List<Location> routeGeometry = List.of(Location.create(20.0, 10.0), Location.create(30.0, 20.0));

        RouteCalculationResult calculationResult = new RouteCalculationResult(
                orderedSensors,
                30.0,
                45L,
                routeGeometry
        );

        Mockito.when(truckRepository.findById(truckId)).thenReturn(Mono.just(truck));
        Mockito.when(sensorRepository.findById(1L)).thenReturn(Mono.just(sensor1));
        Mockito.when(sensorRepository.findById(2L)).thenReturn(Mono.just(sensor2));
        Mockito.when(routeCalculatorService.calculateRoute(sensors)).thenReturn(Mono.just(calculationResult));

        Route route = Route.create(null, truck, orderedSensors, 30.0, 45L, routeGeometry);
        RouteDTO expectedRouteDTO = RouteDTO.fromRoute(route);

        Mockito.when(routeRepository.save(Mockito.any(Route.class))).thenReturn(Mono.just(route));

        StepVerifier.create(routeService.createRoute(request))
                .expectNextMatches(dto -> dto.equals(expectedRouteDTO))
                .verifyComplete();

        Mockito.verify(truckRepository).findById(truckId);
        Mockito.verify(sensorRepository).findById(1L);
        Mockito.verify(sensorRepository).findById(2L);
        Mockito.verify(routeCalculatorService).calculateRoute(sensors);
        Mockito.verify(routeRepository).save(Mockito.any(Route.class));

    }

}
