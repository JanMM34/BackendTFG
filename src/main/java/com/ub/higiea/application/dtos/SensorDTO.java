package com.ub.higiea.application.dtos;

import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.model.ContainerState;
import org.springframework.data.geo.Point;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class SensorDTO implements Serializable {

    private final UUID id;
    private final Point location;
    private final ContainerState containerState;
    private final String routeId;

    private SensorDTO(UUID id, Point location, ContainerState containerState, String routeId) {
        this.id = id;
        this.location = location;
        this.containerState = containerState;
        this.routeId = routeId;
    }

    public static SensorDTO fromSensor(Sensor sensor) {
        String routeId = sensor.hasAssignedRoute() ? sensor.getAssignedRoute().getId() : null;

        return new SensorDTO(
                sensor.getId(),
                new Point(sensor.getLocation().getLatitude(),sensor.getLocation().getLongitude()),
                sensor.getContainerState(),
                routeId
        );
    }

    public UUID getId() {
        return id;
    }

    public Point getLocation() {
        return location;
    }

    public String getRouteId() {
        return routeId;
    }

    public ContainerState getContainerState() {
        return containerState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensorDTO sensorDTO = (SensorDTO) o;
        return Objects.equals(id, sensorDTO.id) &&
                Objects.equals(location, sensorDTO.location) &&
                containerState == sensorDTO.containerState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, containerState);
    }

}
