package com.ub.higiea.application.dtos;

import com.ub.higiea.domain.model.Truck;
import org.springframework.data.geo.Point;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class TruckDTO implements Serializable {

    private final UUID id;
    private final String plate;
    private final String routeId;
    private final int maxLoadCapacity;
    private final Point depotLocation;

    private TruckDTO(UUID id,String plate, String routeId, int maxLoadCapacity, Point depotLocation) {
        this.id = id;
        this.plate = plate;
        this.routeId = routeId;
        this.maxLoadCapacity = maxLoadCapacity;
        this.depotLocation = depotLocation;
    }

    public static TruckDTO fromTruck(Truck truck) {
        String routeId = truck.hasAssignedRoute() ? truck.getAssignedRoute().getId() : null;

        return new TruckDTO(
                truck.getId(),
                truck.getPlate(),
                routeId,
                truck.getMaxLoadCapacity(),
                new Point(
                        truck.getDepotLocation().getLatitude(),
                        truck.getDepotLocation().getLongitude()
                )
        );
    }

    public UUID getId() {
        return id;
    }

    public String getPlate() {
        return this.plate;
    }

    public String getRouteId() {
        return routeId;
    }

    public int getMaxLoadCapacity() {
        return maxLoadCapacity;
    }

    public Point getDepotLocation() {
        return depotLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TruckDTO truckDTO = (TruckDTO) o;
        return Objects.equals(this.plate, truckDTO.plate) &&
                Objects.equals(this.routeId, truckDTO.routeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(plate, routeId);
    }

}
