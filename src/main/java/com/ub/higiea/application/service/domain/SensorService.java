package com.ub.higiea.application.service.domain;

import com.ub.higiea.application.dtos.SensorDTO;
import com.ub.higiea.application.requests.SensorCreateRequest;
import com.ub.higiea.application.exception.notfound.SensorNotFoundException;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.repository.SensorRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public Flux<SensorDTO> getAllSensors() {
        return sensorRepository.findAll()
                .map(SensorDTO::fromSensor);
    }

    public Mono<SensorDTO> getSensorById(Long id) {
        return sensorRepository.findById(id)
                .switchIfEmpty(Mono.error(new SensorNotFoundException(id)))
                .map(SensorDTO::fromSensor);
    }

    public Mono<SensorDTO> createSensor(SensorCreateRequest request) {
        Sensor sensor = Sensor.create(
                null,
                Location.create(request.getLatitude(), request.getLongitude()),
                request.getContainerState()
        );
        return sensorRepository.save(sensor)
                .map(SensorDTO::fromSensor);
    }

}
