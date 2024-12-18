package com.ub.higiea.application.services;

import com.ub.higiea.application.services.domain.RouteService;
import com.ub.higiea.application.services.domain.SensorService;
import com.ub.higiea.application.services.domain.TruckService;
import com.ub.higiea.application.strategies.RoutePlanningStrategy;
import com.ub.higiea.application.strategies.RouteTriggerStrategy;
import com.ub.higiea.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.util.function.Tuples;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private SensorService sensorService;

    @Mock
    private TruckService truckService;

    @Mock
    private RouteService routeService;

    @Mock
    private RouteTriggerStrategy routeTriggerStrategy;

    @Mock
    private RoutePlanningStrategy routePlanningStrategy;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(sensorService, truckService, routeService, routeTriggerStrategy, routePlanningStrategy);
    }

    @Test
    void handleMessage_ShouldTriggerRoute_WhenConditionMet() {
        UUID sensorId = UUID.randomUUID();
        int state = 80;

        Sensor updatedSensor = Sensor.create(sensorId, Location.create(10.0, 20.0), ContainerState.FULL);

        when(sensorService.updateSensorState(sensorId, state))
                .thenReturn(Mono.just(updatedSensor));

        when(routeTriggerStrategy.shouldTriggerRoute(updatedSensor))
                .thenReturn(Mono.just(true));

        when(routePlanningStrategy.prepareRoute())
                .thenReturn(Mono.just(Tuples.of(
                        Truck.create(UUID.randomUUID(), "1", 150, Location.create(30.0, 40.0)),
                        List.of(updatedSensor)
                )));

        when(routeService.calculateAndSaveRoute(any(Truck.class), anyList()))
                .thenReturn(Mono.just(Route.create("route1", null, List.of(updatedSensor), 100.0, 200L, List.of())));

        when(sensorService.assignRouteToSensors(anyList(),any(Route.class))).thenReturn(Flux.empty());
        when(truckService.assignRouteToTruck(any(Truck.class), any(Route.class))).thenReturn(Mono.empty());

        Mono<Void> result = messageService.handleMessage(sensorId, state);

        StepVerifier.create(result)
                .verifyComplete();

        verify(sensorService, times(1)).updateSensorState(sensorId, state);
        verify(routeTriggerStrategy, times(1)).shouldTriggerRoute(updatedSensor);
        verify(routePlanningStrategy, times(1)).prepareRoute();
        verify(routeService, times(1)).calculateAndSaveRoute(any(Truck.class), anyList());
        verify(sensorService, times(1)).assignRouteToSensors(anyList(),any(Route.class));
        verify(truckService, times(1)).assignRouteToTruck(any(Truck.class), any(Route.class));
    }

    @Test
    void handleMessage_ShouldNotTriggerRoute_WhenConditionNotMet() {
        UUID sensorId = UUID.randomUUID();
        int state = 80;

        Sensor updatedSensor = Sensor.create(sensorId, Location.create(10.0, 20.0), ContainerState.FULL);

        when(sensorService.updateSensorState(sensorId, state))
                .thenReturn(Mono.just(updatedSensor));

        when(routeTriggerStrategy.shouldTriggerRoute(updatedSensor))
                .thenReturn(Mono.just(false));

        Mono<Void> result = messageService.handleMessage(sensorId, state);

        StepVerifier.create(result)
                .verifyComplete();

        verify(sensorService, times(1)).updateSensorState(sensorId, state);
        verify(routeTriggerStrategy, times(1)).shouldTriggerRoute(updatedSensor);
        verifyNoInteractions(routePlanningStrategy, routeService, truckService);
    }

}
