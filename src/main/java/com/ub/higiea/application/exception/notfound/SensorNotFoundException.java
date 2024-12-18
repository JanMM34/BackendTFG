package com.ub.higiea.application.exception.notfound;

import java.util.UUID;

public class SensorNotFoundException extends NotFoundException {
    public SensorNotFoundException(UUID id) {
        super("Sensor with id " + id + " not found");
    }
}
