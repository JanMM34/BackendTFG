package com.ub.higiea.application.exception.notfound;

import java.util.UUID;

public class TruckNotFoundException extends RuntimeException {
    public TruckNotFoundException(UUID id) {
        super("Truck with id " + id + " not found");
    }
}
