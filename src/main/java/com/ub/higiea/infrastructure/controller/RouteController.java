package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.service.domain.RouteService;
import com.ub.higiea.application.dtos.RouteDTO;
import com.ub.higiea.application.exception.notfound.RouteNotFoundException;
import com.ub.higiea.application.requests.RouteCreateRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
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
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<RouteDTO> getAllRoutes() {
        return routeService.getAllRoutes();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<RouteDTO> getRoute(@PathVariable("id") String routeId) {
        return routeService.getRouteById(routeId)
                .onErrorMap(RouteNotFoundException.class, ex ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(),ex));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<RouteDTO> createRoute(@Valid @RequestBody RouteCreateRequest request) {
        return routeService.createRoute(request)
                .onErrorMap(ValidationException.class, ex ->
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex));
    }
}
