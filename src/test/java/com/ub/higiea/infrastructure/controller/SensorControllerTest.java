package com.ub.higiea.infrastructure.controller;

import com.ub.higiea.application.dtos.SensorDTO;
import com.ub.higiea.application.service.domain.SensorService;
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

@WebFluxTest(controllers = SensorController.class)
public class SensorControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SensorService sensorService;

    @Test
    void getAllSensors_ShouldReturnListOfSensorDTOs() {

        Sensor sensor1 = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        Sensor sensor2 = Sensor.create(2L, Location.create(30.0, 60.0), ContainerState.EMPTY);

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

        Sensor sensor = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);

        SensorDTO expectedSensorDTO = SensorDTO.fromSensor(sensor);

        Mockito.when(sensorService.getSensorById(1L)).thenReturn(Mono.just(expectedSensorDTO));

        webTestClient.get()
                .uri("/sensors/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SensorDTO.class)
                .isEqualTo(expectedSensorDTO);
    }

    @Test
    void createSensor_ShouldReturnCreatedSensorDTO() {
        SensorCreateRequest sensorCreateRequest = SensorCreateRequest.toRequest(
                10.0,
                20.0,
                ContainerState.EMPTY
        );
        Sensor sensor = Sensor.create(1L, Location.create(20.0, 10.0), ContainerState.EMPTY);
        SensorDTO sensorDTO = SensorDTO.fromSensor(sensor);

        Mockito.when(sensorService.createSensor(Mockito.argThat(request ->
                request.getLatitude().equals(sensorCreateRequest.getLatitude()) &&
                        request.getLongitude().equals(sensorCreateRequest.getLongitude()) &&
                        request.getContainerState() == sensorCreateRequest.getContainerState()
        ))).thenReturn(Mono.just(sensorDTO));

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
        Long sensorId = 1L;
        Mockito.when(sensorService.getSensorById(sensorId))
                .thenReturn(Mono.error(new SensorNotFoundException(sensorId)));

        webTestClient.get()
                .uri("/sensors/{id}", sensorId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Sensor with id 1 not found");
    }

    @Test
    void createSensor_ShouldReturnBadRequest_WhenInputIsInvalid() {
        SensorCreateRequest invalidRequest = SensorCreateRequest.toRequest(
                null,
                20.0,
                ContainerState.EMPTY
        );

        webTestClient.post()
                .uri("/sensors")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }
}