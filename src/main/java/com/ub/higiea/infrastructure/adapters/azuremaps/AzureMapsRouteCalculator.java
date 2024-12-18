package com.ub.higiea.infrastructure.adapters.azuremaps;

import com.azure.core.models.GeoPosition;
import com.azure.maps.route.MapsRouteAsyncClient;
import com.azure.maps.route.models.*;
import com.ub.higiea.application.strategies.RouteCalculationResult;
import com.ub.higiea.application.strategies.RouteCalculator;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AzureMapsRouteCalculator implements RouteCalculator {

    private final MapsRouteAsyncClient mapsRouteClient;

    public AzureMapsRouteCalculator(MapsRouteAsyncClient mapsRouteClient) {
        this.mapsRouteClient = mapsRouteClient;
    }

    @Override
    public Mono<RouteCalculationResult> calculateRoute(Location depotBase, List<Sensor> sensors) {
        return calculateOptimizedRoute(depotBase, sensors)
                .map(routeDirections -> {

                    // Initialize ordered sensors list with depot base at start and end
                    List<Sensor> orderedSensors = new ArrayList<>();
                    orderedSensors.add(null); // Placeholder for depot base at the start
                    for (int i = 0; i < sensors.size(); i++) {
                        orderedSensors.add(null); // Initialize with nulls for sensors
                    }
                    orderedSensors.add(null); // Placeholder for depot base at the end

                    // Extract optimized waypoints
                    List<RouteOptimizedWaypoint> optimizedWaypoints = routeDirections.getOptimizedWaypoints();

                    // Adjust indices and place sensors in the correct order
                    for (RouteOptimizedWaypoint waypoint : optimizedWaypoints) {
                        int providedIndex = waypoint.getProvidedIndex();
                        int optimizedIndex = waypoint.getOptimizedIndex() + 1; // Increment by 1 to account for the origin

                        // Move the sensor from the provided index to the new optimized index
                        Sensor sensorToMove = sensors.get(providedIndex);
                        orderedSensors.set(optimizedIndex, sensorToMove);
                    }

                    // Optionally, set the depot base at the start and end if you have a Sensor object for it
                    // Sensor depotSensor = createDepotSensor(depotBase);
                    // orderedSensors.set(0, depotSensor);
                    // orderedSensors.set(orderedSensors.size() - 1, depotSensor);

                    Double totalDistance = routeDirections.getRoutes().getFirst().getSummary()
                            .getLengthInMeters().doubleValue();

                    Long estimatedTimeInSeconds = routeDirections.getRoutes().getFirst().getSummary()
                            .getTravelTimeInSeconds().getSeconds();

                    List<GeoPosition> routeGeometryPoints = routeDirections.getRoutes().getFirst()
                            .getLegs().stream()
                            .flatMap(leg -> leg.getPoints().stream())
                            .collect(Collectors.toList());

                    List<Location> routeGeometry = routeGeometryPoints.stream()
                            .map(geoPosition -> Location.create(geoPosition.getLatitude(), geoPosition.getLongitude()))
                            .toList();

                    return new RouteCalculationResult(orderedSensors, totalDistance, estimatedTimeInSeconds, routeGeometry);
                });
    }

    private Mono<RouteDirections> calculateOptimizedRoute(Location depotBase, List<Sensor> sensors) {

        List<GeoPosition> coordinates = new ArrayList<>();

        // Add depot base as start point
        coordinates.add(new GeoPosition(depotBase.getLongitude(), depotBase.getLatitude()));

        // Add sensor locations
        coordinates.addAll(sensors.stream()
                .map(sensor -> new GeoPosition(
                        sensor.getLocation().getLongitude(),
                        sensor.getLocation().getLatitude()))
                .collect(Collectors.toList()));

        // Add depot base as end point
        coordinates.add(new GeoPosition(depotBase.getLongitude(), depotBase.getLatitude()));

        RouteDirectionsOptions options = new RouteDirectionsOptions(coordinates)
                .setComputeBestWaypointOrder(true)
                .setTravelMode(TravelMode.TRUCK)
                .setRouteType(RouteType.SHORTEST);

        return mapsRouteClient.getRouteDirections(options)
                .flatMap(response -> {
                    if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
                        return Mono.error(new RuntimeException("No route found"));
                    }
                    return Mono.just(response);
                });
    }

}