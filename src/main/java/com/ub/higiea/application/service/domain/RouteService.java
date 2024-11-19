package com.ub.higiea.application.service.domain;

import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
import com.ub.higiea.application.requests.RouteCreateRequest;
import com.ub.higiea.application.service.external.RouteCalculatorService;
import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
import com.ub.higiea.domain.model.Route;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.model.Truck;
import com.ub.higiea.domain.repository.RouteRepository;
import com.ub.higiea.domain.repository.SensorRepository;
import com.ub.higiea.domain.repository.TruckRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class RouteService {

    private final RouteCalculatorService routeCalculatorService;
    private final RouteRepository routeRepository;
    private final TruckRepository truckRepository;
    private final SensorRepository sensorRepository;

    public RouteService(RouteCalculatorService routeCalculatorService, RouteRepository routeRepository,
                        TruckRepository truckRepository, SensorRepository sensorRepository) {
        this.routeCalculatorService = routeCalculatorService;
        this.routeRepository = routeRepository;
        this.truckRepository = truckRepository;
        this.sensorRepository = sensorRepository;
    }

    public Flux<RouteDTO> getAllRoutes() {
        return routeRepository.findAll()
                .map(RouteDTO::fromRoute);
    }

    public Mono<RouteDTO> getRouteById(String routeId) {
        return routeRepository.findById(routeId)
                .switchIfEmpty(Mono.error(new RouteNotFoundException(routeId)))
                .map(RouteDTO::fromRoute);
    }

    public Mono<RouteDTO> createRoute(RouteCreateRequest request) {
        return Mono.zip(
                        fetchTruck(request.getTruckId()),
                        fetchSensors(request.getSensorIds())
                )
                .flatMap(tuple -> calculateAndSaveRoute(tuple.getT1(), tuple.getT2()))
                .map(RouteDTO::fromRoute);
    }

    private Mono<Truck> fetchTruck(Long truckId) {
        return truckRepository.findById(truckId)
                .switchIfEmpty(Mono.error(new TruckNotFoundException(truckId)));
    }

    private Mono<List<Sensor>> fetchSensors(List<Long> sensorIds) {
        return Flux.fromIterable(sensorIds)
                .flatMap(sensorRepository::findById)
                .collectList()
                .flatMap(sensors -> {
                    if (sensors.size() != sensorIds.size()) {
                        return Mono.error(new IllegalArgumentException("One or more sensors not found."));
                    }
                    return Mono.just(sensors);
                });
    }

    private Mono<Route> calculateAndSaveRoute(Truck truck, List<Sensor> sensors) {
        return routeCalculatorService.calculateRoute(sensors)
                .flatMap(result -> {
                    Route route = Route.create(
                            null,
                            truck,
                            result.getOrderedSensors(),
                            result.getTotalDistance(),
                            result.getEstimatedTimeInSeconds(),
                            result.getRouteGeometry()
                    );
                    return routeRepository.save(route);
                });
    }

}