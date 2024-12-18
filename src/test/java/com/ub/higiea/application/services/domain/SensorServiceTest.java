package com.ub.higiea.application.services.domain;

import com.ub.higiea.application.dtos.SensorDTO;
import com.ub.higiea.application.requests.SensorCreateRequest;
import com.ub.higiea.application.exception.notfound.SensorNotFoundException;
import com.ub.higiea.domain.model.ContainerState;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.repository.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorService sensorService;

    @Test
    void getAllSensors_ShouldReturnAllSensorDTOs_WhenRepositoryNotEmpty() {
        Sensor sensor1 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(UUID.randomUUID(), Location.create(30.0, 20.0), ContainerState.FULL);

        when(sensorRepository.findAll()).thenReturn(Flux.just(sensor1, sensor2));

        StepVerifier.create(sensorService.getAllSensors())
                .expectNextMatches(dto -> dto.equals(SensorDTO.fromSensor(sensor1)))
                .expectNextMatches(dto -> dto.equals(SensorDTO.fromSensor(sensor2)))
                .verifyComplete();

        verify(sensorRepository, times(1)).findAll();
    }

    @Test
    void getAllSensors_ShouldReturnEmptyFlux_WhenRepositoryIsEmpty() {
        when(sensorRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(sensorService.getAllSensors())
                .verifyComplete();

        verify(sensorRepository, times(1)).findAll();
    }

    @Test
    void getSensorById_ShouldReturnSensorDTO_WhenSensorFound() {
        Sensor sensor = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);

        when(sensorRepository.findById(sensor.getId())).thenReturn(Mono.just(sensor));

        StepVerifier.create(sensorService.getSensorById(sensor.getId()))
                .expectNextMatches(dto -> dto.equals(SensorDTO.fromSensor(sensor)))
                .verifyComplete();

        verify(sensorRepository, times(1)).findById(sensor.getId());
    }

    @Test
    void getSensorById_ShouldReturnError_WhenSensorNotFound() {
        UUID sensorId = UUID.randomUUID();
        when(sensorRepository.findById(sensorId)).thenReturn(Mono.empty());

        StepVerifier.create(sensorService.getSensorById(sensorId))
                .expectErrorMatches(throwable -> throwable instanceof SensorNotFoundException)
                .verify();

        verify(sensorRepository, times(1)).findById(sensorId);
    }

    @Test
    void createSensor_ShouldReturnSensorDTO_WhenValidInput() {
        SensorCreateRequest request = SensorCreateRequest.toRequest(20.0, 10.0, "EMPTY");
        Sensor sensor = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);

        when(sensorRepository.save(any(Sensor.class))).thenReturn(Mono.just(sensor));

        StepVerifier.create(sensorService.createSensor(request))
                .expectNextMatches(dto -> dto.equals(SensorDTO.fromSensor(sensor)))
                .verifyComplete();

        ArgumentCaptor<Sensor> sensorCaptor = ArgumentCaptor.forClass(Sensor.class);
        verify(sensorRepository).save(sensorCaptor.capture());
        Sensor savedSensor = sensorCaptor.getValue();

        assertEquals(request.getLatitude(), savedSensor.getLocation().getLatitude());
        assertEquals(request.getLongitude(), savedSensor.getLocation().getLongitude());
        assertEquals(ContainerState.valueOf(request.getContainerState()), savedSensor.getContainerState());
    }

    @Test
    void fetchRelevantSensors_ShouldPrioritizeSensorsByContainerState() {

        int capacity = 5;

        Sensor sensorFull1 = Sensor.create(UUID.randomUUID(), Location.create(10.0, 20.0), ContainerState.FULL);
        Sensor sensorHalf1 = Sensor.create(UUID.randomUUID(), Location.create(15.0, 25.0), ContainerState.HALF);
        Sensor sensorEmpty1 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 30.0), ContainerState.EMPTY);
        Sensor sensorEmpty2 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 30.0), ContainerState.EMPTY);
        Sensor sensorFull2 = Sensor.create(UUID.randomUUID(), Location.create(25.0, 35.0), ContainerState.FULL);
        Sensor sensorHalf2 = Sensor.create(UUID.randomUUID(), Location.create(30.0, 40.0), ContainerState.HALF);

        when(sensorRepository.findUnassignedSensorsSortedByPriority())
                .thenReturn(Flux.just(sensorFull1, sensorFull2, sensorHalf1, sensorHalf2));


        Flux<Sensor> result = sensorService.fetchSensorsByPriorityState();

        StepVerifier.create(result)
                .expectNext(sensorFull1)
                .expectNext(sensorFull2)
                .expectNext(sensorHalf1)
                .expectNext(sensorHalf2)
                .verifyComplete();

        verify(sensorRepository, times(1)).findUnassignedSensorsSortedByPriority();
    }

}
