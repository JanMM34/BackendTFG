package com.ub.higiea.infrastructure.persistence.mapper;

import com.ub.higiea.domain.model.*;
import com.ub.higiea.infrastructure.persistence.entities.RouteEntity;
import org.springframework.data.mongodb.core.geo.GeoJsonLineString;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.geo.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RouteMapper {

    public static RouteEntity toEntity(Route route) {

        String routeId = (route.getId() != null && !route.getId().isEmpty()) ? route.getId()
                : UUID.randomUUID().toString();
        Long truckId = route.getTruck().getId();

        GeoJsonLineString routeGeometry = new GeoJsonLineString(
                route.getRouteGeometry().stream()
                        .map(location -> new Point(location.getLongitude(), location.getLatitude()))
                        .toList()
        );

        Map<String, Object> routeProperties = Map.of(
                "totalDistance", route.getTotalDistance(),
                "estimatedTime", route.getEstimatedTimeInSeconds()
        );

        RouteEntity.RouteFeature routeFeature = new RouteEntity.RouteFeature(
                routeProperties,
                routeGeometry
        );

        List<RouteEntity.SensorFeature> sensorFeatures = route.getSensors().stream()
                .map(sensor -> {
                    Map<String, Object> sensorProperties = Map.of(
                            "sensorId", sensor.getId(),
                            "containerState", sensor.getContainerState().name()
                    );
                    return new RouteEntity.SensorFeature(
                            sensorProperties,
                            new GeoJsonPoint(sensor.getLocation().getLongitude(), sensor.getLocation().getLatitude())
                    );
                })
                .toList();

        List<RouteEntity.Feature> features = new ArrayList<>();
        features.add(routeFeature);
        features.addAll(sensorFeatures);

        return new RouteEntity(routeId, truckId, features);
    }

    public static Route toDomain(RouteEntity entity) {
        String routeId = (entity.getId() != null && !entity.getId().isEmpty()) ? entity.getId() : UUID.randomUUID().toString();
        Truck truck = Truck.create(entity.getTruckId());

        RouteEntity.RouteFeature routeFeature = null;
        List<RouteEntity.SensorFeature> sensorFeatures = new ArrayList<>();

        for (RouteEntity.Feature feature : entity.getFeatures()) {
            if (feature instanceof RouteEntity.RouteFeature) {
                routeFeature = (RouteEntity.RouteFeature) feature;
            } else if (feature instanceof RouteEntity.SensorFeature) {
                sensorFeatures.add((RouteEntity.SensorFeature) feature);
            }
        }

        Double totalDistance = (Double) routeFeature.getProperties().get("totalDistance");
        Long estimatedTimeInSeconds = (Long) routeFeature.getProperties().get("estimatedTime");

        List<Location> routeGeometry = ((GeoJsonLineString) routeFeature.getGeometry()).getCoordinates().stream()
                .map(point -> Location.create(point.getY(), point.getX()))
                .collect(Collectors.toList());


        List<Sensor> sensors = sensorFeatures.stream()
                .map(sensorFeature -> Sensor.create(
                        (Long) sensorFeature.getProperties().get("sensorId"),
                        Location.create(
                                ((GeoJsonPoint) sensorFeature.getGeometry()).getY(),
                                ((GeoJsonPoint) sensorFeature.getGeometry()).getX()
                        ),
                        ContainerState.valueOf((String) sensorFeature.getProperties().get("containerState"))
                ))
                .collect(Collectors.toList());

        return Route.create(
                routeId,
                truck,
                sensors,
                totalDistance,
                estimatedTimeInSeconds,
                routeGeometry
        );

    }

}