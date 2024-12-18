package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.dtos.RouteSummaryDTO;
import com.ub.higiea.application.services.domain.RouteService;
import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("routes")
@Validated
@CrossOrigin
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<RouteSummaryDTO> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getRoute(@PathVariable("id") String routeId) {
        return routeService.getRouteById(routeId)
                .map(RouteDTO::toGeoJSON)
                .onErrorMap(RouteNotFoundException.class, ex ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(),ex));
    }

}
