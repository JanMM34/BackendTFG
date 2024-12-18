package com.ub.higiea.domain.model;

import java.util.UUID;

public class Sensor {

    private UUID id;

    private Location location;

    private ContainerState containerState;

    private Route assignedRoute;

    private Sensor() {
    }

    private Sensor(UUID id, Location location, ContainerState containerState, Route assignedRoute) {
        this.id = id;
        this.location = location;
        this.containerState = containerState;
        this.assignedRoute = assignedRoute;
    }

    public static Sensor create(UUID id, Location location, ContainerState containerState) {
        return new Sensor(id, location, containerState,null);
    }

    public UUID getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public ContainerState getContainerState() {
        return containerState;
    }

    public void setContainerState(ContainerState containerState) {
        this.containerState = containerState;
    }

    public Route getAssignedRoute() {
        return assignedRoute;
    }

    public void assignRoute(Route route) {
        if (this.assignedRoute != null) {
            throw new IllegalStateException("Sensor is already assigned to a assignedRoute");
        }
        this.assignedRoute = route;
    }

    public void unassignRoute() {
        if (this.assignedRoute == null) {
            throw new IllegalStateException("Sensor does not have an assigned route");
        }
        this.assignedRoute = null;
    }

    public boolean hasAssignedRoute() {
        return this.assignedRoute != null;
    }

}