package com.ub.higiea.infrastructure.adapters.azuremaps;

import com.azure.core.models.GeoPosition;
import com.azure.maps.route.MapsRouteAsyncClient;
import com.azure.maps.route.models.*;
import com.ub.higiea.application.responses.RouteCalculationResult;
import com.ub.higiea.domain.model.Location;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.adapter.RouteCalculatorAdapter;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;


public class AzureMapsRouteCalculatorAdapter implements RouteCalculatorAdapter {

    private final MapsRouteAsyncClient mapsRouteClient;

    public AzureMapsRouteCalculatorAdapter(MapsRouteAsyncClient mapsRouteClient) {
        this.mapsRouteClient = mapsRouteClient;
    }

    @Override
    public Mono<RouteCalculationResult> calculateRoute(List<Sensor> sensors) {
        return calculateOptimizedRoute(sensors)
                .map(routeDirections -> {

                    List<Sensor> orderedSensors = new ArrayList<>(sensors);
                    // Extract optimized waypoints
                    List<RouteOptimizedWaypoint> optimizedWaypoints = routeDirections.getOptimizedWaypoints();

                    // Iterate over optimized waypoints and place them in the correct order
                    for (RouteOptimizedWaypoint waypoint : optimizedWaypoints) {
                        int providedIndex = waypoint.getProvidedIndex();
                        int optimizedIndex = waypoint.getOptimizedIndex();

                        // Move the sensor from the provided index to the new optimized index
                        Sensor sensorToMove = sensors.get(providedIndex);
                        orderedSensors.set(optimizedIndex, sensorToMove);
                    }

                    Double totalDistance = routeDirections.getRoutes().getFirst().getSummary()
                            .getLengthInMeters().doubleValue();

                    Long estimatedTimeInSeconds = routeDirections.getRoutes().getFirst().getSummary()
                            .getTravelTimeInSeconds().getSeconds();

                    List<GeoPosition> routeGeometryPoints = routeDirections.getRoutes().getFirst()
                            .getLegs().stream()
                            .flatMap(leg -> leg.getPoints().stream())
                            .toList();

                    List<Location> routeGeometry = routeGeometryPoints.stream()
                            .map(geoPosition -> Location.create(geoPosition.getLatitude(), geoPosition.getLongitude()))
                            .toList();

                    return new RouteCalculationResult(orderedSensors, totalDistance, estimatedTimeInSeconds, routeGeometry);
                });
    }

    private Mono<RouteDirections> calculateOptimizedRoute(List<Sensor> sensors) {
        List<GeoPosition> coordinates = sensors.stream()
                .map(sensor -> new GeoPosition(
                        sensor.getLocation().getLongitude(),
                        sensor.getLocation().getLatitude()))
                .toList();

        RouteDirectionsOptions options = new RouteDirectionsOptions(coordinates)
                .setComputeBestWaypointOrder(true)
                .setRouteType(RouteType.FASTEST);

        return mapsRouteClient.getRouteDirections(options)
                .flatMap(response -> {
                    if (response.getRoutes() == null || response.getRoutes().isEmpty()) {
                        return Mono.error(new RuntimeException("No route found"));
                    }
                    return Mono.just(response);
                });
    }

}