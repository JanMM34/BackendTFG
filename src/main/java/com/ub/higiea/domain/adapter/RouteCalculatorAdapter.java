package com.ub.higiea.domain.adapter;

import com.ub.higiea.application.responses.RouteCalculationResult;
import com.ub.higiea.domain.model.Sensor;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RouteCalculatorAdapter {

    Mono<RouteCalculationResult> calculateRoute(List<Sensor> sensors);

}
