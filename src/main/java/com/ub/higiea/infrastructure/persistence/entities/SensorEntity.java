package com.ub.higiea.infrastructure.persistence.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import java.util.UUID;

@Table("sensor")
public class SensorEntity{

    @Id
    private UUID id;

    @Column("latitude")
    private Double latitude;

    @Column("longitude")
    private Double longitude;

    @Column("state")
    private String containerState;

    @Column("assigned_route")
    private String routeId;

    public SensorEntity() {
    }

    public SensorEntity(UUID uuid,Double latitude, Double longitude, String containerState) {
        this.id = uuid;
        this.latitude = latitude;
        this.longitude = longitude;
        this.containerState = containerState;
    }

    public UUID getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getContainerState() {
        return containerState;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

}