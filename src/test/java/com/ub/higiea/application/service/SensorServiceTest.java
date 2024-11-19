package com.ub.higiea.application.service;

import com.ub.higiea.application.dtos.SensorDTO;
import com.ub.higiea.application.requests.SensorCreateRequest;
import com.ub.higiea.application.exception.notfound.SensorNotFoundException;
import com.ub.higiea.application.service.domain.SensorService;
import com.ub.higiea.domain.model.ContainerState;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.repository.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class SensorServiceTest {

    private SensorService sensorService;
    private SensorRepository sensorRepository;

    @BeforeEach
    public void setUp() {
        sensorRepository = Mockito.mock(SensorRepository.class);
        sensorService = new SensorService(sensorRepository);
    }

    @Test
    void getAllSensors_ShouldReturnAllSensorsDTOs_WhenRepositoryNotEmpty() {
        Sensor sensor1 = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(2L, Location.create(30.0, 20.0), ContainerState.FULL);

        SensorDTO expectedSensor1DTO = SensorDTO.fromSensor(sensor1);
        SensorDTO expectedSensor2DTO = SensorDTO.fromSensor(sensor2);

        Mockito.when(sensorRepository.findAll()).thenReturn(Flux.just(sensor1, sensor2));

        StepVerifier.create(sensorService.getAllSensors())
                .expectNextMatches(dto -> dto.equals(expectedSensor1DTO))
                .expectNextMatches(dto -> dto.equals(expectedSensor2DTO))
                .verifyComplete();
    }

    @Test
    void getAllSensors_shouldReturnEmptyFlux_whenRepositoryIsEmpty() {
        Mockito.when(sensorRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(sensorService.getAllSensors())
                .verifyComplete();
    }

    @Test
    void getSensorById_shouldReturnSensorDTO_WhenSensorFound() {
        Sensor sensor = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        SensorDTO expectedSensorDTO = SensorDTO.fromSensor(sensor);

        Mockito.when(sensorRepository.findById(sensor.getId())).thenReturn(Mono.just(sensor));

        StepVerifier.create(sensorService.getSensorById(sensor.getId()))
                .expectNextMatches(dto -> dto.equals(expectedSensorDTO))
                .verifyComplete();
    }

    @Test
    void getSensorById_shouldReturnError_whenSensorNotFound() {
        Long sensorId = 999L;
        Mockito.when(sensorRepository.findById(sensorId)).thenReturn(Mono.empty());

        StepVerifier.create(sensorService.getSensorById(sensorId))
                .expectError(SensorNotFoundException.class)
                .verify();
    }

    @Test
    void createSensor_shouldReturnSensorDTO_WhenValidInput() {
        SensorCreateRequest request = SensorCreateRequest.toRequest(
                10.0, 20.0,
                ContainerState.EMPTY
        );
        Sensor sensor = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);

        SensorDTO expectedSensorDTO = SensorDTO.fromSensor(sensor);

        Mockito.when(sensorRepository.save(Mockito.any(Sensor.class))).thenReturn(Mono.just(sensor));

        StepVerifier.create(sensorService.createSensor(request))
                .expectNextMatches(dto -> dto.equals(expectedSensorDTO))
                .verifyComplete();
    }

}