package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.dtos.SensorDTO;
import com.ub.higiea.application.services.domain.SensorService;
import com.ub.higiea.application.requests.SensorCreateRequest;
import com.ub.higiea.application.exception.notfound.SensorNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("sensors")
@Validated
@CrossOrigin
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<SensorDTO> getAllSensors() {
        return sensorService.getAllSensors();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SensorDTO> getSensor(@PathVariable("id") UUID sensorId) {
        return sensorService.getSensorById(sensorId)
                .onErrorMap(SensorNotFoundException.class, ex ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SensorDTO> createSensor(@Valid @RequestBody SensorCreateRequest sensorCreateRequest) {
        return sensorService.createSensor(sensorCreateRequest)
                .onErrorMap(ValidationException.class, ex -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        ex.getMessage(), ex));
    }

}
