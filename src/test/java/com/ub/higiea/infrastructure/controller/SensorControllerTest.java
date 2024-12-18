package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.dtos.SensorDTO;
import com.ub.higiea.application.services.domain.SensorService;
import com.ub.higiea.application.requests.SensorCreateRequest;
import com.ub.higiea.application.exception.notfound.SensorNotFoundException;
import com.ub.higiea.domain.model.ContainerState;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@WebFluxTest(controllers = SensorController.class)
public class SensorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SensorService sensorService;

    @Test
    void getAllSensors_ShouldReturnListOfSensorDTOs() {

        Sensor sensor1 = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(UUID.randomUUID(), Location.create(30.0, 60.0), ContainerState.FULL);

        SensorDTO sensorDTO1 = SensorDTO.fromSensor(sensor1);
        SensorDTO sensorDTO2 = SensorDTO.fromSensor(sensor2);

        Mockito.when(sensorService.getAllSensors()).thenReturn(Flux.just(sensorDTO1, sensorDTO2));

        webTestClient.get().uri("/sensors")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SensorDTO.class)
                .hasSize(2)
                .contains(sensorDTO1, sensorDTO2);
    }

    @Test
    void getSensorById_ShouldReturnSensorDTO_WhenSensorExists() {
        UUID sensorId = UUID.randomUUID();
        Sensor sensor = Sensor.create(sensorId, Location.create(20.0, 10.0), ContainerState.EMPTY);
        SensorDTO expectedSensorDTO = SensorDTO.fromSensor(sensor);

        Mockito.when(sensorService.getSensorById(sensorId)).thenReturn(Mono.just(expectedSensorDTO));

        webTestClient.get()
                .uri("/sensors/{id}", sensorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SensorDTO.class)
                .isEqualTo(expectedSensorDTO);
    }

    @Test
    void createSensor_ShouldReturnCreatedSensorDTO() {
        SensorCreateRequest sensorCreateRequest = SensorCreateRequest.toRequest(20.0, 10.0, "EMPTY");
        Sensor sensor = Sensor.create(UUID.randomUUID(), Location.create(20.0, 10.0), ContainerState.EMPTY);
        SensorDTO sensorDTO = SensorDTO.fromSensor(sensor);

        Mockito.when(sensorService.createSensor(Mockito.any(SensorCreateRequest.class))).thenReturn(Mono.just(sensorDTO));

        webTestClient.post()
                .uri("/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sensorCreateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SensorDTO.class)
                .isEqualTo(sensorDTO);
    }

    @Test
    void getSensorById_ShouldReturnNotFound_WhenSensorNotFound() {
        UUID sensorId = UUID.randomUUID();
        Mockito.when(sensorService.getSensorById(sensorId))
                .thenReturn(Mono.error(new SensorNotFoundException(sensorId)));

        webTestClient.get()
                .uri("/sensors/{id}", sensorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void createSensor_ShouldReturnBadRequest_WhenInputIsInvalid() {
        SensorCreateRequest invalidRequest = SensorCreateRequest.toRequest(null, 20.0, "EMPTY");

        webTestClient.post()
                .uri("/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

}