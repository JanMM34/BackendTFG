package com.ub.higiea.application.services;

import com.ub.higiea.application.strategies.implementations.FullHalfRoutePlanningStrategy;
import com.ub.higiea.application.services.domain.SensorService;
import com.ub.higiea.application.services.domain.TruckService;
import com.ub.higiea.domain.model.ContainerState;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.model.Truck;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FullHalfRoutePlanningStrategyTest {

    @Mock
    private SensorService sensorService;

    @Mock
    private TruckService truckService;

    private FullHalfRoutePlanningStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FullHalfRoutePlanningStrategy(sensorService, truckService);
    }

    @Test
    void prepareRoute_optimalTruckFound() {
        List<Sensor> sensors = Arrays.asList(
                createSensor(ContainerState.FULL),
                createSensor(ContainerState.HALF)
        );

        when(sensorService.fetchSensorsByPriorityState()).thenReturn(Flux.fromIterable(sensors));

        Truck optimalTruck = createTruck(130, "OPTIMAL_TRUCK");

        when(truckService.fetchOptimalTruck(
                sensors.get(0).getContainerState().getLevel()+
                        sensors.get(1).getContainerState().getLevel())
        ).thenReturn(Mono.just(optimalTruck));


        Mono<Tuple2<Truck, List<Sensor>>> result = strategy.prepareRoute();


        StepVerifier.create(result)
                .expectNextMatches(tuple -> tuple.getT1().equals(optimalTruck) && tuple.getT2().equals(sensors))
                .expectComplete()
                .verify();
    }

    @Test
    void prepareRoute_noOptimalTruckButBiggestTruckFoundAndSensorsAdjusted() {
        List<Sensor> sensors = Arrays.asList(
                createSensor(ContainerState.FULL),
                createSensor(ContainerState.FULL),
                createSensor(ContainerState.HALF)
        );
        when(sensorService.fetchSensorsByPriorityState()).thenReturn(Flux.fromIterable(sensors));

        when(truckService.fetchOptimalTruck(210)).thenReturn(Mono.empty());

        Truck biggestTruck = createTruck(200, "BIGGEST_TRUCK");
        when(truckService.fetchBiggestTruck()).thenReturn(Mono.just(biggestTruck));


        Mono<Tuple2<Truck, List<Sensor>>> result = strategy.prepareRoute();


        StepVerifier.create(result)
                .expectNextMatches(tuple -> {
                    Truck returnedTruck = tuple.getT1();
                    List<Sensor> returnedSensors = tuple.getT2();
                    return returnedTruck.equals(biggestTruck) &&
                            returnedSensors.size() == 2 &&
                            returnedSensors.stream().mapToInt(s -> s.getContainerState().getLevel()).sum() == 160;
                })
                .expectComplete()
                .verify();
    }

    private Sensor createSensor(ContainerState state) {
        return Sensor.create(UUID.randomUUID(),Location.create(30.0, 40.0), state);
    }

    private Truck createTruck(int capacity, String name) {
        return Truck.create(UUID.randomUUID(), name, capacity, Location.create(30.0, 40.0));
    }
}
