package com.ub.higiea.application.services.domain;

import com.ub.higiea.application.dtos.RouteSummaryDTO;
import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
import com.ub.higiea.application.strategies.RouteCalculator;
import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.domain.model.Route;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.model.Truck;
import com.ub.higiea.domain.repository.RouteRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class RouteService {

    private final RouteCalculator routeCalculator;
    private final RouteRepository routeRepository;

    public RouteService(RouteCalculator routeCalculator, RouteRepository routeRepository) {
        this.routeCalculator = routeCalculator;
        this.routeRepository = routeRepository;
    }

    public Flux<RouteSummaryDTO> getAllRoutes() {
        return routeRepository.findAll()
                .map(RouteSummaryDTO::fromRoute);
    }

    public Mono<RouteDTO> getRouteById(String routeId) {
        return routeRepository.findById(routeId)
                .switchIfEmpty(Mono.error(new RouteNotFoundException(routeId)))
                .map(RouteDTO::fromRoute);
    }

    public Mono<Route> getRouteEntityById(String routeId) {
        return routeRepository.findById(routeId)
                .switchIfEmpty(Mono.error(new RouteNotFoundException(routeId)));
    }

    public Mono<Route> calculateAndSaveRoute(Truck truck, List<Sensor> sensorWaypoints) {
        return routeCalculator.calculateRoute(truck.getDepotLocation(), sensorWaypoints)
                .flatMap(result -> {
                    Route route = Route.create(
                            null,
                            truck,
                            result.orderedSensors(),
                            result.totalDistance(),
                            result.estimatedTimeInSeconds(),
                            result.routeGeometry()
                    );
                    return routeRepository.save(route);
                });
    }

    public Mono<Boolean> checkIfLastSensor(Sensor sensor) {
        return this.routeRepository.findById(sensor.getAssignedRoute().getId())
                .map(route -> route.getSensors().getLast().getId().equals(sensor.getId()))
                .defaultIfEmpty(false);

    }

}