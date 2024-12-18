package com.ub.higiea.infrastructure.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("truck")
public class TruckEntity {

    @Id
    private UUID id;

    @Column("plate")
    private String plate;

    @Column("route")
    private String routeId;

    @Column("max_load_capacity")
    private int maxLoadCapacity;

    @Column("depot_latitude")
    private Double depotLatitude;

    @Column("depot_longitude")
    private Double depotLongitude;

    public TruckEntity() {
    }

    public TruckEntity(UUID id, String plate, int maxLoadCapacity, Double depotLatitude, Double depotLongitude) {
        this.id = id;
        this.plate = plate;
        this.maxLoadCapacity = maxLoadCapacity;
        this.depotLatitude = depotLatitude;
        this.depotLongitude = depotLongitude;
    }

    public UUID getId() {
        return id;
    }

    public String getPlate() {
        return this.plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public boolean isAvailable() {
        return this.routeId == null || this.routeId.isEmpty();
    }

    public int getMaxLoadCapacity() {
        return this.maxLoadCapacity;
    }

    public Double getDepotLatitude() {
        return depotLatitude;
    }

    public Double getDepotLongitude() {
        return depotLongitude;
    }

    public void setMaxLoadCapacity(int maxLoadCapacity) {
        this.maxLoadCapacity = maxLoadCapacity;
    }

    public void setDepotLatitude(Double depotLatitude) {
        this.depotLatitude = depotLatitude;
    }

    public void setDepotLongitude(Double depotLongitude) {
        this.depotLongitude = depotLongitude;
    }

}
