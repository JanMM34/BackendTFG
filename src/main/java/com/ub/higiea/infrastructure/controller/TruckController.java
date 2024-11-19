package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.service.domain.TruckService;
import com.ub.higiea.application.dtos.TruckDTO;
import com.ub.higiea.application.exception.notfound.TruckNotFoundException;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("trucks")
@Validated
public class TruckController {

    private final TruckService truckService;

    public TruckController(TruckService truckService) {
        this.truckService = truckService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<TruckDTO> getAllTrucks() {
        return truckService.getAllTrucks();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TruckDTO> getTruck(@PathVariable("id") Long truckId) {
        return truckService.getTruckById(truckId)
                .onErrorMap(TruckNotFoundException.class, ex ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND,ex.getMessage(),ex));
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<TruckDTO> createTruck() {
        return truckService.createTruck()
                .onErrorMap(ValidationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        ex.getMessage(), ex));
    }

}
