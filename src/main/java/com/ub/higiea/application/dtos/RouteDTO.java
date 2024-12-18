package com.ub.higiea.application.dtos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ub.higiea.domain.model.Route;
import org.springframework.data.geo.Point;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class RouteDTO implements Serializable {

    private final String id;
    private final TruckDTO truck;
    private final List<SensorDTO> sensors;
    private final Double totalDistance;
    private final Long estimatedTime;
    private final List<Point> routeGeometry;

    private RouteDTO(String id, TruckDTO truck, List<SensorDTO> sensors, Double totalDistance, Long estimatedTime, List<Point> routeGeometry) {
        this.id = id;
        this.truck = truck;
        this.sensors = sensors;
        this.totalDistance = totalDistance;
        this.estimatedTime = estimatedTime;
        this.routeGeometry = routeGeometry;
    }

    public static RouteDTO fromRoute(Route route) {
        return new RouteDTO(
                route.getId(),
                TruckDTO.fromTruck(route.getTruck()),
                route.getSensors().stream().map(SensorDTO::fromSensor).toList(),
                route.getTotalDistance(),
                route.getEstimatedTimeInSeconds(),
                route.getRouteGeometry().stream()
                        .map(location ->
                                new Point(location.getLongitude(), location.getLatitude())
                        ).toList()
        );
    }

    public String getId() {
        return id;
    }

    public TruckDTO getTruck() {
        return truck;
    }

    public List<SensorDTO> getSensors() {
        return sensors;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public Long getEstimatedTime() {
        return estimatedTime;
    }

    public List<Point> getRouteGeometry() {
        return routeGeometry;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteDTO routeDTO = (RouteDTO) o;
        return Objects.equals(id, routeDTO.id) &&
                Objects.equals(truck, routeDTO.truck) &&
                Objects.equals(sensors, routeDTO.sensors) &&
                Objects.equals(totalDistance, routeDTO.totalDistance) &&
                Objects.equals(estimatedTime, routeDTO.estimatedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, truck, sensors, totalDistance, estimatedTime);
    }

    public String toGeoJSON() {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode featureCollection = mapper.createObjectNode();
        featureCollection.put("type", "FeatureCollection");
        featureCollection.put("id",this.id);
        featureCollection.put("truckId", this.truck.getPlate());

        ArrayNode featuresArray = mapper.createArrayNode();

        ObjectNode routeFeature = mapper.createObjectNode();
        routeFeature.put("type", "Feature");

        ObjectNode routeProperties = mapper.createObjectNode();
        routeProperties.put("totalDistance", this.totalDistance);
        routeProperties.put("estimatedTime", this.estimatedTime);
        routeFeature.set("properties", routeProperties);

        ObjectNode routeGeometryNode = mapper.createObjectNode();
        routeGeometryNode.put("type", "LineString");

        Point depotLocation = this.truck.getDepotLocation();


        ObjectNode depotFeature = mapper.createObjectNode();
        depotFeature.put("type", "Feature");

        ObjectNode depotProperties = mapper.createObjectNode();
        depotProperties.put("type", "depot");
        depotFeature.set("properties", depotProperties);

        ObjectNode depotGeometry = mapper.createObjectNode();
        depotGeometry.put("type", "Point");

        ArrayNode depotCoordinates = mapper.createArrayNode();
        depotCoordinates.add(depotLocation.getY());
        depotCoordinates.add(depotLocation.getX());
        depotGeometry.set("coordinates", depotCoordinates);
        depotFeature.set("geometry", depotGeometry);

        featuresArray.add(depotFeature);

        for (SensorDTO sensor : this.sensors) {
            ObjectNode sensorFeature = mapper.createObjectNode();
            sensorFeature.put("type", "Feature");

            ObjectNode sensorProperties = mapper.createObjectNode();
            sensorProperties.put("sensorId", sensor.getId().toString());
            sensorProperties.put("containerState", sensor.getContainerState().name());
            sensorFeature.set("properties", sensorProperties);

            ObjectNode sensorGeometry = mapper.createObjectNode();
            sensorGeometry.put("type", "Point");

            ArrayNode sensorCoordinates = mapper.createArrayNode();
            sensorCoordinates.add(sensor.getLocation().getY());
            sensorCoordinates.add(sensor.getLocation().getX());
            sensorGeometry.set("coordinates", sensorCoordinates);
            sensorFeature.set("geometry", sensorGeometry);

            featuresArray.add(sensorFeature);
        }

        ArrayNode coordinatesArray = mapper.createArrayNode();
        for (Point location : this.routeGeometry) {
            ArrayNode coordinate = mapper.createArrayNode();
            coordinate.add(location.getX());
            coordinate.add(location.getY());
            coordinatesArray.add(coordinate);
        }
        routeGeometryNode.set("coordinates", coordinatesArray);
        routeFeature.set("geometry", routeGeometryNode);

        featuresArray.add(routeFeature);


        featureCollection.set("features", featuresArray);

        try {
            return mapper.writeValueAsString(featureCollection);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}