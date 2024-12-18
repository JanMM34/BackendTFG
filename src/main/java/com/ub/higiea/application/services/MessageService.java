package com.ub.higiea.application.services;

import com.ub.higiea.application.services.domain.RouteService;
import com.ub.higiea.application.strategies.RoutePlanningStrategy;
import com.ub.higiea.application.strategies.RouteTriggerStrategy;
import com.ub.higiea.application.services.domain.SensorService;
import com.ub.higiea.application.services.domain.TruckService;
import com.ub.higiea.domain.model.ContainerState;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.model.Truck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final SensorService sensorService;
    private final TruckService truckService;
    private final RouteService routeService;
    private final RouteTriggerStrategy routeTriggerStrategy;
    private final RoutePlanningStrategy routePlanningStrategy;

    public MessageService(SensorService sensorService, TruckService truckService, RouteService routeService,
                          RouteTriggerStrategy routeTriggerStrategy, RoutePlanningStrategy routePlanningStrategy) {

        this.sensorService = sensorService;
        this.truckService = truckService;
        this.routeService = routeService;
        this.routeTriggerStrategy = routeTriggerStrategy;
        this.routePlanningStrategy = routePlanningStrategy;
    }

    public Mono<Void> handleMessage(UUID sensorId, int state) {
        logger.info("Handling message for sensor ID: {}, state: {}", sensorId, state);
        return sensorService.updateSensorState(sensorId, state)
                .flatMap(sensor -> {
                    if (sensor.getContainerState() == ContainerState.EMPTY) {
                        return handleEmptySensor(sensor);
                    } else {
                        return routeTriggerStrategy.shouldTriggerRoute(sensor)
                                .flatMap(shouldTrigger -> {
                                    if (shouldTrigger) {
                                        logger.info("Route trigger condition met. Triggering route.");
                                        return triggerRoute();
                                    }
                                    return Mono.empty();
                                });
                    }
                })
                .doOnSuccess(result -> logger.info("Message handling completed successfully for sensor ID: {}", sensorId))
                .doOnError(error -> logger.error("Error handling message for sensor ID: {}", sensorId, error))
                .then();
    }

    private Mono<Void> handleEmptySensor(Sensor sensor) {
        return routeService.checkIfLastSensor(sensor)
                .flatMap(isLast -> {
                    if (isLast) {
                        logger.info("Sensor {} is the last sensor. Handling last sensor.", sensor);
                        return handleLastSensor(sensor);
                    }
                    return Mono.empty();
                })
                .then(sensorService.markSensorUnassigned(sensor))
                .doOnSuccess(unused -> logger.info("Successfully marked sensor as unassigned: {}", sensor))
                .doOnError(error -> logger.error("Error marking sensor as unassigned: {}", sensor, error));
    }

    private Mono<Void> handleLastSensor(Sensor sensor) {
        return routeService.getRouteEntityById(sensor.getAssignedRoute().getId())
                .flatMap(route -> truckService.unassignRouteFromTruck(route.getTruck().getId())).then();
    }

    private Mono<Void> triggerRoute() {
        return routePlanningStrategy.prepareRoute()
                .flatMap(tuple -> {
                    Truck truck = tuple.getT1();
                    List<Sensor> sensors = tuple.getT2();
                    return routeService.calculateAndSaveRoute(truck, sensors)
                            .flatMap(route -> Mono.when(
                                    sensorService.assignRouteToSensors(sensors,route),
                                    truckService.assignRouteToTruck(truck,route)
                            ));
                })
                .then();
    }

}
