package com.ub.higiea.application.responses;

import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;

import java.util.List;

public class RouteCalculationResult {
    private final List<Sensor> orderedSensors;
    private final Double totalDistance;
    private final Long estimatedTimeInSeconds;
    private final List<Location> routeGeometry;

    public RouteCalculationResult(List<Sensor> orderedSensors, Double totalDistance, Long estimatedTimeInSeconds, List<Location> routeGeometry) {
        this.orderedSensors = orderedSensors;
        this.totalDistance = totalDistance;
        this.estimatedTimeInSeconds = estimatedTimeInSeconds;
        this.routeGeometry = routeGeometry;
    }

    public List<Sensor> getOrderedSensors() {
        return orderedSensors;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public Long getEstimatedTimeInSeconds() {
        return estimatedTimeInSeconds;
    }

    public List<Location> getRouteGeometry() {
        return routeGeometry;
    }
}