package com.ub.higiea.application.service.external;

import com.ub.higiea.application.responses.RouteCalculationResult;
import com.ub.higiea.domain.model.Sensor;
import com.ub.higiea.domain.adapter.RouteCalculatorAdapter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RouteCalculatorService {
    private RouteCalculatorAdapter routeCalculatorAdapter;
    public RouteCalculatorService(RouteCalculatorAdapter routeCalculatorAdapter) {
        this.routeCalculatorAdapter = routeCalculatorAdapter;
    }

    public Mono<RouteCalculationResult> calculateRoute(List<Sensor> sensors) {
        return routeCalculatorAdapter.calculateRoute(sensors);
    }

}
