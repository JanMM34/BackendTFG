package com.ub.higiea.application.strategies;

import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;

import java.util.List;

public record RouteCalculationResult(
        List<Sensor> orderedSensors,
        Double totalDistance,
        Long estimatedTimeInSeconds,
        List<Location> routeGeometry
){}
