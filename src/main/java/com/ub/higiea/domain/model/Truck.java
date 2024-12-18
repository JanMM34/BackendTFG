package com.ub.higiea.domain.model;

import org.springframework.data.annotation.Transient;

import java.util.UUID;

public class Truck {

    private UUID id;

    private String plate;

    private Route assignedRoute;

    private final int maxLoadCapacity;

    private Location depotLocation;

    private Truck(UUID id, String plate, Route assignedRoute, int maxLoadCapacity, Location depotLocation) {
        this.id = id;
        this.plate = plate;
        this.assignedRoute = assignedRoute;
        this.maxLoadCapacity = maxLoadCapacity;
        this.depotLocation = depotLocation;
    }

    public static Truck create(UUID id, String plate, int maxLoadCapacity, Location depotLocation) {
        return new Truck(id, plate, null, maxLoadCapacity, depotLocation);
    }

    public String getPlate() {
        return this.plate;
    }

    public int getMaxLoadCapacity() {
        return maxLoadCapacity;
    }

    public Route getAssignedRoute() {
        return assignedRoute;
    }

    public void assignRoute(Route route) {
        if (this.assignedRoute != null) {
            throw new IllegalStateException("Truck is already assigned to a assignedRoute");
        }
        this.assignedRoute = route;
    }

    public void unassignRoute() {
        if (this.assignedRoute == null) {
            throw new IllegalStateException("Truck does not have an assigned route");
        }
        this.assignedRoute = null;
    }

    public boolean hasAssignedRoute() {
        return this.assignedRoute != null;
    }

    public Location getDepotLocation() {
        return depotLocation;
    }

    public UUID getId() {
        return id;
    }
}